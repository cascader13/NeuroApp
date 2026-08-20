package com.neuroproject.neuro.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.neuroproject.neuro.data.remote.*
import com.neuroproject.neuro.data.session.SessionDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для пакетной отправки метрик на сервер.
 *
 * Управляет подготовкой, разбиением на пакеты и отправкой данных.
 * Поддерживает повторные попытки и контроль прогресса.
 *
 * Основные возможности:
 * - Пакетная отправка с настраиваемым размером пакета
 * - Повторные попытки при ошибках сети
 * - Пометка успешно отправленных данных (isMarked)
 * - Сохранение неудачных запросов для отладки
 * - Получение статистики по данным
 *
 * @see BatchUploadProgress
 * @see MetricBatch
 */
@Singleton
class MetricsUploadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val metricsDao: MetricsDao,
    private val sessionDao: SessionDao,
    private val gson: Gson,
    private val apiService: MetricsApiService
) {

    companion object {
        private const val DEFAULT_BATCH_SIZE = 100 // Записей на пакет
        private const val MAX_BATCH_SIZE_BYTES = 1024 * 1024 // 1 MB
        private const val RETRY_DELAY_MS = 2000L // 2 секунды
        private const val MAX_RETRIES = 3
        private const val BATCH_DELAY_MS = 500L // Задержка между пакетами
        private const val TAG = "MetricsUploadRepository"
    }

    /**
     * Защита от одновременного запуска ручной синхронизации и WorkManager.
     * Без этого два upload-процесса могут прочитать один и тот же набор isMarked=0
     * и параллельно отправить дубликаты на backend.
     */
    private val syncMutex = Mutex()

    // ==================== НОВАЯ РЕАЛИЗАЦИЯ С ПАКЕТНОЙ ОТПРАВКОЙ ====================

    /**
     * Пакетная отправка данных с контролем прогресса
     *
     * @param batchSize Максимальное количество записей в одном пакете
     * @param enableRetry Включить повторные попытки для неудачных пакетов
     * @param stopOnError Останавливать отправку при ошибке или продолжать
     * @param batchDelayMs Задержка между пакетами в миллисекундах
     */
    suspend fun uploadInBatches(
        batchSize: Int = DEFAULT_BATCH_SIZE,
        enableRetry: Boolean = true,
        stopOnError: Boolean = true,
        batchDelayMs: Long = BATCH_DELAY_MS
    ): Flow<BatchUploadProgress> = flow {
        syncMutex.lock()
        try {
            // Шаг 1: Подготовка данных
            emit(BatchUploadProgress.Preparing("Подготовка данных...", 0.05f))

            val preparationResult = prepareDataForUpload()

            when (preparationResult) {
                is UploadPreparationResult.NoData -> {
                    emit(BatchUploadProgress.NoData)
                    return@flow
                }
                is UploadPreparationResult.Error -> {
                    emit(BatchUploadProgress.Error(preparationResult.message))
                    return@flow
                }
                is UploadPreparationResult.Ready -> {
                    // Шаг 2: Разбиение на пакеты
                    emit(BatchUploadProgress.Preparing("Разбиение на пакеты...", 0.1f))

                    val batches = splitIntoBatches(
                        request = preparationResult.request,
                        maxRecordsPerBatch = batchSize,
                        maxBatchSizeBytes = MAX_BATCH_SIZE_BYTES
                    )

                    if (batches.isEmpty()) {
                        emit(BatchUploadProgress.NoData)
                        return@flow
                    }

                    emit(BatchUploadProgress.BatchesCreated(
                        totalBatches = batches.size,
                        totalRecords = preparationResult.totalRecords
                    ))

                    // Шаг 3: Отправка пакетов
                    var successfullySent = 0
                    val failedBatches = mutableListOf<MetricBatch>()

                    for ((index, batch) in batches.withIndex()) {
                        emit(BatchUploadProgress.SendingBatch(
                            current = index + 1,
                            total = batches.size,
                            recordsInBatch = batch.recordCount,
                            batchId = batch.batchId
                        ))

                        val result = sendBatchWithRetry(
                            batch = batch,
                            maxRetries = if (enableRetry) MAX_RETRIES else 1
                        )

                        when (result) {
                            is BatchSendResult.Success -> {
                                // Помечаем данные как отправленные
                                markBatchAsUploaded(batch)
                                successfullySent += batch.recordCount

                                emit(BatchUploadProgress.BatchCompleted(
                                    batchIndex = index + 1,
                                    totalBatches = batches.size,
                                    recordsInBatch = batch.recordCount,
                                    totalSentSoFar = successfullySent,
                                    batchId = batch.batchId
                                ))
                            }
                            is BatchSendResult.Failure -> {
                                emit(BatchUploadProgress.BatchFailed(
                                    batchIndex = index + 1,
                                    totalBatches = batches.size,
                                    error = result.message,
                                    batchId = batch.batchId,
                                    willRetry = false
                                ))

                                if (stopOnError) {
                                    // Останавливаем отправку при первой ошибке
                                    emit(BatchUploadProgress.Stopped(
                                        sentCount = successfullySent,
                                        failedCount = batch.recordCount,
                                        reason = result.message
                                    ))
                                    return@flow
                                } else {
                                    failedBatches.add(batch)
                                }
                            }
                        }

                        // Задержка между пакетами для снижения нагрузки
                        if (index < batches.size - 1) {
                            delay(batchDelayMs)
                        }
                    }

                    // Шаг 4: Обработка неудачных пакетов
                    if (failedBatches.isNotEmpty()) {
                        emit(BatchUploadProgress.PartialSuccess(
                            sentCount = successfullySent,
                            totalCount = preparationResult.totalRecords,
                            failedBatches = failedBatches.size,
                            failedRecords = failedBatches.sumOf { it.recordCount }
                        ))
                    } else {
                        emit(BatchUploadProgress.Completed(
                            sentCount = successfullySent,
                            totalBatches = batches.size
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            emit(BatchUploadProgress.Error("Критическая ошибка: ${e.message}"))
        } finally {
            syncMutex.unlock()
        }
    }

    /**
     * Отправка одного пакета с повторными попытками
     */
    private suspend fun sendBatchWithRetry(
        batch: MetricBatch,
        maxRetries: Int
    ): BatchSendResult {
        var lastException: Exception? = null

        for (attempt in 1..maxRetries) {
            try {
                // ЛОГИРУЕМ РАЗМЕР ПАКЕТА
                val jsonString = gson.toJson(batch.request)
                Log.d(TAG, "=== BATCH ${batch.batchId} ===")
                Log.d(TAG, "JSON size: ${jsonString.length} bytes")
                Log.d(TAG, "Records: ${batch.recordCount}")

                val response = apiService.uploadMetrics(batch.request)

                if (response.isSuccessful && response.body()?.result == true) {
                    Log.i(TAG, "Batch ${batch.batchId} sent successfully")
                    return BatchSendResult.Success(batch.batchId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.w(TAG, "Batch ${batch.batchId} FAILED with ${response.code()}")
                    Log.w(TAG, "Error response: $errorBody")

                    // СОХРАНЯЕМ ПРОБЛЕМНЫЙ JSON В ФАЙЛ
                    saveFailedJson(batch.request, batch.batchId, response.code(), errorBody)

                    // АНАЛИЗИРУЕМ СОДЕРЖИМОЕ ПАКЕТА
                    analyzeBatchContent(batch.request)

                    val errorMessage = when {
                        response.isSuccessful -> "Сервер вернул result=false. Сохранён в failed_requests/"
                        response.code() == 400 -> "Некорректный запрос. Сохранён в failed_requests/"
                        else -> "HTTP ${response.code()}: ${response.message()}"
                    }

                    return BatchSendResult.Failure(batch.batchId, errorMessage)
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries) {
                    delay(RETRY_DELAY_MS)
                    continue
                }
                return BatchSendResult.Failure(batch.batchId, "Ошибка: ${e.message}")
            }
        }

        return BatchSendResult.Failure(
            batch.batchId,
            "Превышено количество попыток: ${lastException?.message ?: "Неизвестная ошибка"}"
        )
    }

    private fun saveFailedJson(request: UploadRequest, batchId: String, code: Int, errorBody: String?) {
        try {
            val exportDir = File(context.filesDir, "failed_requests")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "failed_${code}_${batchId}_$timestamp.json"
            val jsonFile = File(exportDir, fileName)

            val fullLog = mapOf(
                "batchId" to batchId,
                "httpCode" to code,
                "serverError" to errorBody,
                "timestamp" to timestamp,
                "requestData" to request
            )

            val jsonString = gson.toJson(fullLog)
            jsonFile.writeText(jsonString)

            Log.i(TAG, "Failed request saved to: ${jsonFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save JSON: ${e.message}", e)
        }
    }

    private fun analyzeBatchContent(request: UploadRequest) {
        Log.d(TAG, "=== BATCH CONTENT ANALYSIS ===")

        // Проверяем каждый тип данных
        request.nfbMetrics?.let {
            Log.d(TAG, "NFB metrics: ${it.size} records")
            if (it.isNotEmpty()) {
                val sample = it.first()
                Log.d(TAG, "  Sample: timestamp=${sample.timestamp}, alpha=${sample.alpha}, beta=${sample.beta}")
                // Проверяем валидность значений
                if (sample.individualNumber.isBlank()) Log.w(TAG, "  ⚠️ WARNING: empty individualNumber!")
                if (sample.expeditionId.isBlank()) Log.w(TAG, "  ⚠️ WARNING: empty expeditionId!")
                if (sample.timestamp <= 0) Log.w(TAG, "  ⚠️ WARNING: invalid timestamp!")
            }
        } ?: Log.d(TAG, "NFB metrics: null")

        request.physiologicalMetrics?.let {
            Log.d(TAG, "Physiological metrics: ${it.size} records")
            if (it.isNotEmpty()) {
                val sample = it.first()
                Log.d(TAG, "  Sample: relax=${sample.relax}, concentration=${sample.concentration}, stress=${sample.stress}")
                // Проверяем диапазоны
                if (sample.relax !in 0.0..1.0) Log.w(TAG, "  ⚠️ WARNING: relax out of range [0-1]: ${sample.relax}")
                if (sample.concentration !in 0.0..1.0) Log.w(TAG, "  ⚠️ WARNING: concentration out of range: ${sample.concentration}")
            }
        } ?: Log.d(TAG, "Physiological metrics: null")

        request.cardioMetrics?.let {
            Log.d(TAG, "Cardio metrics: ${it.size} records")
            if (it.isNotEmpty()) {
                val sample = it.first()
                Log.d(TAG, "  Sample: heartRate=${sample.heartRate}, skinContact=${sample.skinContact}")
                if (sample.heartRate < 30 || sample.heartRate > 200) Log.w(TAG, "  ⚠️ WARNING: unusual heart rate: ${sample.heartRate}")
            }
        } ?: Log.d(TAG, "Cardio metrics: null")

        // Добавьте другие типы метрик по необходимости

        Log.d(TAG, "=== END ANALYSIS ===")
    }


    /**
     * Разбиение запроса на пакеты
     */
    private fun splitIntoBatches(
        request: UploadRequest,
        maxRecordsPerBatch: Int,
        maxBatchSizeBytes: Int
    ): List<MetricBatch> {
        val batches = mutableListOf<MetricBatch>()
        val allMetrics = mutableListOf<TypedMetric>()

        // Собираем все метрики в единый список с указанием типа
        request.nfbMetrics?.forEach {
            allMetrics.add(TypedMetric.NFB(it))
        }
        request.physiologicalMetrics?.forEach {
            allMetrics.add(TypedMetric.Physiological(it))
        }
        request.EEGRawMetrics?.forEach {
            allMetrics.add(TypedMetric.EEGRaw(it))
        }
        request.EEGProceedMetrics?.forEach {
            allMetrics.add(TypedMetric.EEGProceed(it))
        }
        request.EEGArtifactsMetrics?.forEach {
            allMetrics.add(TypedMetric.EEGArtifact(it))
        }
        request.memsMetrics?.forEach {
            allMetrics.add(TypedMetric.MEMS(it))
        }
        request.productivityMetrics?.forEach {
            allMetrics.add(TypedMetric.Productivity(it))
        }
        request.emotionalMetrics?.forEach {
            allMetrics.add(TypedMetric.Emotional(it))
        }
        request.cardioMetrics?.forEach {
            allMetrics.add(TypedMetric.Cardio(it))
        }
        request.sessionResult?.forEach {
            allMetrics.add(TypedMetric.Session(it))
        }

        // Сжатые данные
        request.nfbMetricsCompressed?.forEach {
            allMetrics.add(TypedMetric.NFBCompressed(it))
        }
        request.physiologicalMetricsCompressed?.forEach {
            allMetrics.add(TypedMetric.PhysiologicalCompressed(it))
        }
        request.EEGRawMetricsCompressed?.forEach {
            allMetrics.add(TypedMetric.EEGRawCompressed(it))
        }
        request.EEGProceedMetricsCompressed?.forEach {
            allMetrics.add(TypedMetric.EEGProceedCompressed(it))
        }
        request.EEGArtifactsMetricsCompressed?.forEach {
            allMetrics.add(TypedMetric.EEGArtifactCompressed(it))
        }
        request.memsMetricsCompressed?.forEach {
            allMetrics.add(TypedMetric.MEMSCompressed(it))
        }
        request.productivityMetricsCompressed?.forEach {
            allMetrics.add(TypedMetric.ProductivityCompressed(it))
        }
        request.emotionalMetricsCompressed?.forEach {
            allMetrics.add(TypedMetric.EmotionalCompressed(it))
        }
        request.cardioMetricsCompressed?.forEach {
            allMetrics.add(TypedMetric.CardioCompressed(it))
        }

        // Базовые значения
        request.physiologicalBaseline?.forEach {
            allMetrics.add(TypedMetric.PhysiologicalBaseline(it))
        }
        request.productivityBaseline?.forEach {
            allMetrics.add(TypedMetric.ProductivityBaseline(it))
        }
        request.productivityIndex?.forEach {
            allMetrics.add(TypedMetric.ProductivityIndex(it))
        }

        // Разбиваем на чанки
        val chunks = mutableListOf<MutableList<TypedMetric>>()
        var currentChunk = mutableListOf<TypedMetric>()
        var currentChunkSizeBytes = 0L

        for (metric in allMetrics) {
            // Оцениваем размер метрики в JSON
            val metricJson = gson.toJson(metric)
            val metricSize = metricJson.toByteArray().size.toLong()

            // Проверяем, не превысит ли добавление лимиты
            if (currentChunk.size >= maxRecordsPerBatch ||
                (currentChunkSizeBytes + metricSize) > maxBatchSizeBytes
            ) {
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk)
                    currentChunk = mutableListOf()
                    currentChunkSizeBytes = 0L
                }
            }

            currentChunk.add(metric)
            currentChunkSizeBytes += metricSize
        }

        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk)
        }

        // Конвертируем чанки обратно в UploadRequest
        for (chunk in chunks) {
            val batchRequest = UploadRequest(
                nfbMetrics = chunk.filterIsInstance<TypedMetric.NFB>().map { it.data }.takeIf { it.isNotEmpty() },
                physiologicalMetrics = chunk.filterIsInstance<TypedMetric.Physiological>().map { it.data }.takeIf { it.isNotEmpty() },
                EEGRawMetrics = chunk.filterIsInstance<TypedMetric.EEGRaw>().map { it.data }.takeIf { it.isNotEmpty() },
                EEGProceedMetrics = chunk.filterIsInstance<TypedMetric.EEGProceed>().map { it.data }.takeIf { it.isNotEmpty() },
                EEGArtifactsMetrics = chunk.filterIsInstance<TypedMetric.EEGArtifact>().map { it.data }.takeIf { it.isNotEmpty() },
                memsMetrics = chunk.filterIsInstance<TypedMetric.MEMS>().map { it.data }.takeIf { it.isNotEmpty() },
                productivityMetrics = chunk.filterIsInstance<TypedMetric.Productivity>().map { it.data }.takeIf { it.isNotEmpty() },
                emotionalMetrics = chunk.filterIsInstance<TypedMetric.Emotional>().map { it.data }.takeIf { it.isNotEmpty() },
                cardioMetrics = chunk.filterIsInstance<TypedMetric.Cardio>().map { it.data }.takeIf { it.isNotEmpty() },
                nfbMetricsCompressed = chunk.filterIsInstance<TypedMetric.NFBCompressed>().map { it.data }.takeIf { it.isNotEmpty() },
                physiologicalMetricsCompressed = chunk.filterIsInstance<TypedMetric.PhysiologicalCompressed>().map { it.data }.takeIf { it.isNotEmpty() },
                EEGRawMetricsCompressed = chunk.filterIsInstance<TypedMetric.EEGRawCompressed>().map { it.data }.takeIf { it.isNotEmpty() },
                EEGProceedMetricsCompressed = chunk.filterIsInstance<TypedMetric.EEGProceedCompressed>().map { it.data }.takeIf { it.isNotEmpty() },
                EEGArtifactsMetricsCompressed = chunk.filterIsInstance<TypedMetric.EEGArtifactCompressed>().map { it.data }.takeIf { it.isNotEmpty() },
                memsMetricsCompressed = chunk.filterIsInstance<TypedMetric.MEMSCompressed>().map { it.data }.takeIf { it.isNotEmpty() },
                productivityMetricsCompressed = chunk.filterIsInstance<TypedMetric.ProductivityCompressed>().map { it.data }.takeIf { it.isNotEmpty() },
                emotionalMetricsCompressed = chunk.filterIsInstance<TypedMetric.EmotionalCompressed>().map { it.data }.takeIf { it.isNotEmpty() },
                cardioMetricsCompressed = chunk.filterIsInstance<TypedMetric.CardioCompressed>().map { it.data }.takeIf { it.isNotEmpty() },
                physiologicalBaseline = chunk.filterIsInstance<TypedMetric.PhysiologicalBaseline>().map { it.data }.takeIf { it.isNotEmpty() },
                productivityBaseline = chunk.filterIsInstance<TypedMetric.ProductivityBaseline>().map { it.data }.takeIf { it.isNotEmpty() },
                productivityIndex = chunk.filterIsInstance<TypedMetric.ProductivityIndex>().map { it.data }.takeIf { it.isNotEmpty() },
                sessionResult = chunk.filterIsInstance<TypedMetric.Session>().map { it.data }.takeIf { it.isNotEmpty() }
            )

            if (batchRequest.hasData()) {
                batches.add(
                    MetricBatch(
                        request = batchRequest,
                        recordCount = chunk.size,
                        batchId = UUID.randomUUID().toString(),
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        return batches
    }

    /**
     * Пометить пакет как отправленный
     */
    private suspend fun markBatchAsUploaded(batch: MetricBatch) {
        // Uncompressed NFB
        batch.request.nfbMetrics?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkNFBMetricsAsSynced(metrics.map { it.rowId })
            }
        }

        // Uncompressed Physiological
        batch.request.physiologicalMetrics?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkPhysiologicalMetricsAsSynced(metrics.map { it.rowId })
            }
        }

        // Uncompressed EEG Raw
        batch.request.EEGRawMetrics?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkEEGRAWMetricsAsSynced(metrics.map { it.rowId })
            }
        }

        // Uncompressed EEG Proceed
        batch.request.EEGProceedMetrics?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkEEGProceedMetricsAsSynced(metrics.map { it.rowId })
            }
        }

        // Uncompressed EEG Artifacts
        batch.request.EEGArtifactsMetrics?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkEEGArtifactsMetricsAsSynced(metrics.map { it.rowId })
            }
        }

        // Uncompressed MEMS
        batch.request.memsMetrics?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkMEMSMetricsAsSynced(metrics.map { it.rowId })
            }
        }

        // Uncompressed Productivity
        batch.request.productivityMetrics?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkProductivityMetricsAsSynced(metrics.map { it.rowId })
            }
        }

        // Uncompressed Emotional
        batch.request.emotionalMetrics?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkEmotionalMetricsAsSynced(metrics.map { it.rowId })
            }
        }

        // Uncompressed Cardio
        batch.request.cardioMetrics?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkCardioMetricsAsSynced(metrics.map { it.rowId })
            }
        }

        // Compressed NFB
        batch.request.nfbMetricsCompressed?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkNFBMetricsCompressedAsSynced(metrics.map { it.rowId })
            }
        }

        // Compressed Physiological
        batch.request.physiologicalMetricsCompressed?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkPhysiologicalMetricsCompressedAsSynced(metrics.map { it.rowId })
            }
        }

        // Compressed EEG Raw
        batch.request.EEGRawMetricsCompressed?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkEEGRAWMetricsCompressedAsSynced(metrics.map { it.rowId })
            }
        }

        // Compressed EEG Proceed
        batch.request.EEGProceedMetricsCompressed?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkEEGProceedMetricsCompressedAsSynced(metrics.map { it.rowId })
            }
        }

        // Compressed EEG Artifacts
        batch.request.EEGArtifactsMetricsCompressed?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkEEGArtifactsMetricsCompressedAsSynced(metrics.map { it.rowId })
            }
        }

        // Compressed MEMS
        batch.request.memsMetricsCompressed?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkMEMSMetricsCompressedAsSynced(metrics.map { it.rowId })
            }
        }

        // Compressed Productivity
        batch.request.productivityMetricsCompressed?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkProductivityMetricsCompressedAsSynced(metrics.map { it.rowId })
            }
        }

        // Compressed Emotional
        batch.request.emotionalMetricsCompressed?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkEmotionalMetricsCompressedAsSynced(metrics.map { it.rowId })
            }
        }

        // Compressed Cardio
        batch.request.cardioMetricsCompressed?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkCardioMetricsCompressedAsSynced(metrics.map { it.rowId })
            }
        }

        // Baselines
        batch.request.physiologicalBaseline?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkPhysiologicalBaselineAsSynced(metrics.map { it.rowId })
            }
        }

        batch.request.productivityBaseline?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkProductivityBaselineAsSynced(metrics.map { it.rowId })
            }
        }

        batch.request.productivityIndex?.let { metrics ->
            if (metrics.isNotEmpty()) {
                metricsDao.safeMarkProductivityIndexesAsSynced(metrics.map { it.rowId })
            }
        }

        //SessionResults
        batch.request.sessionResult?.let {metrics ->
            if (metrics.isNotEmpty()) {
                sessionDao.safeMarkSessionResultAsSynced(metrics.mapNotNull { it.localSessionId })
            }
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Подготовить данные для отправки (оставляем без изменений)
     */
    suspend fun prepareDataForUpload(): UploadPreparationResult {
        return try {
            // Uncompressed данные
            val nfbMetrics = metricsDao.getUnmarkedNFBMetrics()
            val physiologicalMetrics = metricsDao.getUnmarkedPhysiologicalMetrics()
            val eegRawMetrics = metricsDao.getUnmarkedEEGRAWMetrics()
            val eegProceedMetrics = metricsDao.getUnmarkedEEGPROCEEDMetrics()
            val eegArtifactsMetrics = metricsDao.getUnmarkedEEGArtifactsMetrics()
            val memsMetrics = metricsDao.getUnmarkedMEMSMetrics()
            val productivityMetrics = metricsDao.getUnmarkedProductivityMetrics()
            val emotionalMetrics = metricsDao.getUnmarkedEmotionalMetrics()
            val cardioMetrics = metricsDao.getUnmarkedCardioMetrics()
            val sessions = sessionDao.getUnmarkedSessionResult()
                .filter { !it.id.isNullOrBlank() && !it.expedition_id.isNullOrBlank() }

            // Compressed данные
            val nfbMetricsCompressed = metricsDao.getUnmarkedNFBMetricsCompressed()
            val physiologicalMetricsCompressed = metricsDao.getUnmarkedPhysiologicalMetricsCompressed()
            val eegRawMetricsCompressed = metricsDao.getUnmarkedEEGRAWMetricsCompressed()
            val eegProceedMetricsCompressed = metricsDao.getUnmarkedEEGPROCEEDMetricsCompressed()
            val eegArtifactsMetricsCompressed = metricsDao.getUnmarkedEEGArtifactsMetricsCompressed()
            val memsMetricsCompressed = metricsDao.getUnmarkedMEMSMetricsCompressed()
            val productivityMetricsCompressed = metricsDao.getUnmarkedProductivityMetricsCompressed()
            val emotionalMetricsCompressed = metricsDao.getUnmarkedEmotionalMetricsCompressed()
            val cardioMetricsCompressed = metricsDao.getUnmarkedCardioMetricsCompressed()

            // Baseline и Indexes данные
            val physiologicalBaselines = metricsDao.getUnmarkedPhysiologicalBaseline()
            val productivityBaselines = metricsDao.getUnmarkedProductivityBaseline()
            val productivityIndexes = metricsDao.getUnmarkedProductivityIndexes()

            val totalRecords = calculateTotalRecords(
                nfbMetrics, physiologicalMetrics, eegRawMetrics, eegProceedMetrics,
                eegArtifactsMetrics, memsMetrics, productivityMetrics, emotionalMetrics, cardioMetrics,
                nfbMetricsCompressed, physiologicalMetricsCompressed, eegRawMetricsCompressed,
                eegProceedMetricsCompressed, eegArtifactsMetricsCompressed, memsMetricsCompressed,
                productivityMetricsCompressed, emotionalMetricsCompressed, cardioMetricsCompressed,
                physiologicalBaselines, productivityBaselines, productivityIndexes, sessions
            )

            if (totalRecords == 0) {
                return UploadPreparationResult.NoData
            }

            val uploadRequest = UploadRequest(
                nfbMetrics = nfbMetrics.map { it.toServerDto() },
                physiologicalMetrics = physiologicalMetrics.map { it.toServerDto() },
                EEGRawMetrics = eegRawMetrics.map { it.toServerDto() },
                EEGProceedMetrics = eegProceedMetrics.map { it.toServerDto() },
                EEGArtifactsMetrics = eegArtifactsMetrics.map { it.toServerDto() },
                memsMetrics = memsMetrics.map { it.toServerDto() },
                productivityMetrics = productivityMetrics.map { it.toServerDto() },
                emotionalMetrics = emotionalMetrics.map { it.toServerDto() },
                cardioMetrics = cardioMetrics.map { it.toServerDto() },
                nfbMetricsCompressed = nfbMetricsCompressed.map { it.toServerDto() },
                physiologicalMetricsCompressed = physiologicalMetricsCompressed.map { it.toServerDto() },
                EEGRawMetricsCompressed = eegRawMetricsCompressed.map { it.toServerDto() },
                EEGProceedMetricsCompressed = eegProceedMetricsCompressed.map { it.toServerDto() },
                EEGArtifactsMetricsCompressed = eegArtifactsMetricsCompressed.map { it.toServerDto() },
                memsMetricsCompressed = memsMetricsCompressed.map { it.toServerDto() },
                productivityMetricsCompressed = productivityMetricsCompressed.map { it.toServerDto() },
                emotionalMetricsCompressed = emotionalMetricsCompressed.map { it.toServerDto() },
                cardioMetricsCompressed = cardioMetricsCompressed.map { it.toServerDto() },
                physiologicalBaseline = physiologicalBaselines.map { it.toServerDto() },
                productivityBaseline = productivityBaselines.map { it.toServerDto() },
                productivityIndex = productivityIndexes.map { it.toServerDto() },
                sessionResult = sessions.map {it.toServerDto()}
            )

            UploadPreparationResult.Ready(
                request = uploadRequest,
                totalRecords = totalRecords,
                nfbCount = nfbMetrics.size,
                physiologicalCount = physiologicalMetrics.size,
                eegRawCount = eegRawMetrics.size,
                eegProceedCount = eegProceedMetrics.size,
                eegArtifactsCount = eegArtifactsMetrics.size,
                memsCount = memsMetrics.size,
                productivityCount = productivityMetrics.size,
                emotionalCount = emotionalMetrics.size,
                cardioCount = cardioMetrics.size,
                nfbCompressedCount = nfbMetricsCompressed.size,
                physiologicalCompressedCount = physiologicalMetricsCompressed.size,
                eegRawCompressedCount = eegRawMetricsCompressed.size,
                eegProceedCompressedCount = eegProceedMetricsCompressed.size,
                eegArtifactsCompressedCount = eegArtifactsMetricsCompressed.size,
                memsCompressedCount = memsMetricsCompressed.size,
                productivityCompressedCount = productivityMetricsCompressed.size,
                emotionalCompressedCount = emotionalMetricsCompressed.size,
                cardioCompressedCount = cardioMetricsCompressed.size,
                physiologicalBaselinesCount = physiologicalBaselines.size,
                productivityBaselinesCount = productivityBaselines.size,
                productivityIndexesCount = productivityIndexes.size,
                sessionsCount =  sessions.size
            )

        } catch (e: Exception) {
            UploadPreparationResult.Error("Ошибка подготовки данных: ${e.message}")
        }
    }

    private fun calculateTotalRecords(vararg lists: List<*>): Int {
        return lists.sumOf { it.size }
    }

    /**
     * Сохранить данные в JSON файл (для отладки) - обновленная версия с поддержкой пакетов
     */
    suspend fun saveToJsonFile(saveAsBatches: Boolean = false): FileSaveResult {
        return try {
            val preparationResult = prepareDataForUpload()

            when (preparationResult) {
                is UploadPreparationResult.NoData -> FileSaveResult.NoData
                is UploadPreparationResult.Error -> FileSaveResult.Error(preparationResult.message)
                is UploadPreparationResult.Ready -> {
                    val exportDir = File(context.filesDir, "metrics_exports")
                    if (!exportDir.exists()) exportDir.mkdirs()

                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

                    if (saveAsBatches) {
                        // Сохраняем каждый пакет в отдельный файл
                        val batches = splitIntoBatches(
                            request = preparationResult.request,
                            maxRecordsPerBatch = DEFAULT_BATCH_SIZE,
                            maxBatchSizeBytes = MAX_BATCH_SIZE_BYTES
                        )

                        val batchDir = File(exportDir, "batch_export_$timestamp")
                        batchDir.mkdirs()

                        batches.forEachIndexed { index, batch ->
                            val fileName = "batch_${index + 1}_${batch.batchId}.json"
                            val jsonFile = File(batchDir, fileName)
                            val jsonString = gson.toJson(batch.request)
                            jsonFile.writeText(jsonString)
                        }

                        FileSaveResult.Success(
                            filePath = batchDir.absolutePath,
                            fileName = "batch_export_$timestamp",
                            recordsCount = preparationResult.totalRecords
                        )
                    } else {
                        // Сохраняем одним файлом
                        val fileName = "neuro_upload_${timestamp}.json"
                        val jsonFile = File(exportDir, fileName)
                        val jsonString = gson.toJson(preparationResult.request)
                        jsonFile.writeText(jsonString)

                        FileSaveResult.Success(
                            filePath = jsonFile.absolutePath,
                            fileName = fileName,
                            recordsCount = preparationResult.totalRecords
                        )
                    }
                }
            }
        } catch (e: Exception) {
            FileSaveResult.Error("Ошибка сохранения файла: ${e.message}")
        }
    }

    /**
     * Получить статистику (оставляем без изменений)
     */
    suspend fun getStats(): UploadStats {
        // Uncompressed counts
        val nfbCount = metricsDao.getAllNFBMetricsCount()
        val physiologicalCount = metricsDao.getAllPhysiologicalMetricsCount()
        val eegRawCount = metricsDao.getAllEEGRAWMetricsCount()
        val eegProceedCount = metricsDao.getAllEEGPROCEEDMetricsCount()
        val eegArtifactsCount = metricsDao.getAllEEGArtifactsMetricsCount()
        val memsCount = metricsDao.getAllMEMSMetricsCount()
        val productivityCount = metricsDao.getAllProductivityMetricsCount()
        val emotionalCount = metricsDao.getAllEmotionalMetricsCount()
        val cardioCount = metricsDao.getAllCardioMetricsCount()

        // Compressed counts
        val nfbCompressedCount = metricsDao.getAllNFBMetricsCountCompressed()
        val physiologicalCompressedCount = metricsDao.getAllPhysiologicalMetricsCountCompressed()
        val eegRawCompressedCount = metricsDao.getAllEEGRAWMetricsCountCompressed()
        val eegProceedCompressedCount = metricsDao.getAllEEGPROCEEDMetricsCountCompressed()
        val eegArtifactsCompressedCount = metricsDao.getAllEEGArtifactsMetricsCountCompressed()
        val memsCompressedCount = metricsDao.getAllMEMSMetricsCountCompressed()
        val productivityCompressedCount = metricsDao.getAllProductivityMetricsCountCompressed()
        val emotionalCompressedCount = metricsDao.getAllEmotionalMetricsCountCompressed()
        val cardioCompressedCount = metricsDao.getAllCardioMetricsCountCompressed()

        // Baseline/session counts
        val physiologicalBaselinesCount = metricsDao.getAllPhysiologicalBaselineCount()
        val productivityBaselinesCount = metricsDao.getAllProductivityBaselineCount()
        val productivityIndexesCount = metricsDao.getAllProductivityIndexesCount()
        val sessionsCount = sessionDao.getSessionResultCount()

        // Uncompressed unsynced counts
        val nfbUnsynced = metricsDao.getUnmarkedNFBMetricsCount()
        val physiologicalUnsynced = metricsDao.getUnmarkedPhysiologicalMetricsCount()
        val eegRawUnsynced = metricsDao.getUnmarkedEEGRAWMetricsCount()
        val eegProceedUnsynced = metricsDao.getUnmarkedEEGPROCEEDMetricsCount()
        val eegArtifactsUnsynced = metricsDao.getUnmarkedEEGArtifactMetricsCount()
        val memsUnsynced = metricsDao.getUnmarkedMEMSMetricsCount()
        val productivityUnsynced = metricsDao.getUnmarkedProductivityMetricsCount()
        val emotionalUnsynced = metricsDao.getUnmarkedEmotionalMetricsCount()
        val cardioUnsynced = metricsDao.getUnmarkedCardioMetricsCount()

        // Compressed unsynced counts
        val nfbCompressedUnsynced = metricsDao.getUnmarkedNFBMetricsCountCompressed()
        val physiologicalCompressedUnsynced = metricsDao.getUnmarkedPhysiologicalMetricsCountCompressed()
        val eegRawCompressedUnsynced = metricsDao.getUnmarkedEEGRAWMetricsCountCompressed()
        val eegProceedCompressedUnsynced = metricsDao.getUnmarkedEEGPROCEEDMetricsCountCompressed()
        val eegArtifactsCompressedUnsynced = metricsDao.getUnmarkedEEGArtifactMetricsCountCompressed()
        val memsCompressedUnsynced = metricsDao.getUnmarkedMEMSMetricsCountCompressed()
        val productivityCompressedUnsynced = metricsDao.getUnmarkedProductivityMetricsCountCompressed()
        val emotionalCompressedUnsynced = metricsDao.getUnmarkedEmotionalMetricsCountCompressed()
        val cardioCompressedUnsynced = metricsDao.getUnmarkedCardioMetricsCountCompressed()

        // Baseline/session unsynced counts
        val physiologicalBaselinesUnsynced = metricsDao.getUnmarkedPhysiologicalBaseline().size
        val productivityBaselinesUnsynced = metricsDao.getUnmarkedProductivityBaseline().size
        val productivityIndexesUnsynced = metricsDao.getUnmarkedProductivityIndexes().size
        val sessionsUnsynced = sessionDao.getUnmarkedSessionResultCount()

        val totalRecords = nfbCount + physiologicalCount + eegRawCount + eegProceedCount +
                eegArtifactsCount + memsCount + productivityCount + emotionalCount + cardioCount +
                nfbCompressedCount + physiologicalCompressedCount + eegRawCompressedCount +
                eegProceedCompressedCount + eegArtifactsCompressedCount + memsCompressedCount +
                productivityCompressedCount + emotionalCompressedCount + cardioCompressedCount +
                physiologicalBaselinesCount + productivityBaselinesCount + productivityIndexesCount + sessionsCount

        val unsyncedRecords = nfbUnsynced + physiologicalUnsynced + eegRawUnsynced + eegProceedUnsynced +
                eegArtifactsUnsynced + memsUnsynced + productivityUnsynced + emotionalUnsynced + cardioUnsynced +
                nfbCompressedUnsynced + physiologicalCompressedUnsynced + eegRawCompressedUnsynced +
                eegProceedCompressedUnsynced + eegArtifactsCompressedUnsynced + memsCompressedUnsynced +
                productivityCompressedUnsynced + emotionalCompressedUnsynced + cardioCompressedUnsynced +
                physiologicalBaselinesUnsynced + productivityBaselinesUnsynced + productivityIndexesUnsynced + sessionsUnsynced

        return UploadStats(
            totalRecords = totalRecords,
            unsyncedRecords = unsyncedRecords,
            nfbCount = nfbCount,
            physiologicalCount = physiologicalCount,
            eegRawCount = eegRawCount,
            eegProceedCount = eegProceedCount,
            eegArtifactsCount = eegArtifactsCount,
            memsCount = memsCount,
            productivityCount = productivityCount,
            emotionalCount = emotionalCount,
            cardioCount = cardioCount,
            nfbCompressedCount = nfbCompressedCount,
            physiologicalCompressedCount = physiologicalCompressedCount,
            eegRawCompressedCount = eegRawCompressedCount,
            eegProceedCompressedCount = eegProceedCompressedCount,
            eegArtifactsCompressedCount = eegArtifactsCompressedCount,
            memsCompressedCount = memsCompressedCount,
            productivityCompressedCount = productivityCompressedCount,
            emotionalCompressedCount = emotionalCompressedCount,
            cardioCompressedCount = cardioCompressedCount,
            physiologicalBaselinesCount = physiologicalBaselinesCount,
            productivityBaselinesCount = productivityBaselinesCount,
            productivityIndexesCount = productivityIndexesCount,
            sessionsCount = sessionsCount,
            nfbUnsynced = nfbUnsynced,
            physiologicalUnsynced = physiologicalUnsynced,
            eegRawUnsynced = eegRawUnsynced,
            eegProceedUnsynced = eegProceedUnsynced,
            eegArtifactsUnsynced = eegArtifactsUnsynced,
            memsUnsynced = memsUnsynced,
            productivityUnsynced = productivityUnsynced,
            emotionalUnsynced = emotionalUnsynced,
            cardioUnsynced = cardioUnsynced,
            nfbCompressedUnsynced = nfbCompressedUnsynced,
            physiologicalCompressedUnsynced = physiologicalCompressedUnsynced,
            eegRawCompressedUnsynced = eegRawCompressedUnsynced,
            eegProceedCompressedUnsynced = eegProceedCompressedUnsynced,
            eegArtifactsCompressedUnsynced = eegArtifactsCompressedUnsynced,
            memsCompressedUnsynced = memsCompressedUnsynced,
            productivityCompressedUnsynced = productivityCompressedUnsynced,
            emotionalCompressedUnsynced = emotionalCompressedUnsynced,
            cardioCompressedUnsynced = cardioCompressedUnsynced,
            physiologicalBaselinesUnsynced = physiologicalBaselinesUnsynced,
            productivityBaselinesUnsynced = productivityBaselinesUnsynced,
            productivityIndexesUnsynced = productivityIndexesUnsynced,
            sessionsUnsynced = sessionsUnsynced
        )
    }
}

// ==================== НОВЫЕ КЛАССЫ ДЛЯ ПАКЕТНОЙ ОТПРАВКИ ====================

/**
 * Типизированная метрика для удобной группировки
 */
sealed class TypedMetric {
    data class NFB(val data: NfbMetricDto) : TypedMetric()
    data class Physiological(val data: PhysiologicalMetricDto) : TypedMetric()
    data class EEGRaw(val data: EEGRawMetricDto) : TypedMetric()
    data class EEGProceed(val data: EEGProceedMetricDto) : TypedMetric()
    data class EEGArtifact(val data: EEGArtifactMetricDto) : TypedMetric()
    data class MEMS(val data: MemsMetricDto) : TypedMetric()
    data class Productivity(val data: ProductivityMetricDto) : TypedMetric()
    data class Emotional(val data: EmotionalMetricDto) : TypedMetric()
    data class Cardio(val data: CardioMetricDto) : TypedMetric()

    data class NFBCompressed(val data: NfbMetricCompressedDto) : TypedMetric()
    data class PhysiologicalCompressed(val data: PhysiologicalMetricCompressedDto) : TypedMetric()
    data class EEGRawCompressed(val data: EEGRawMetricCompressedDto) : TypedMetric()
    data class EEGProceedCompressed(val data: EEGProceedMetricCompressedDto) : TypedMetric()
    data class EEGArtifactCompressed(val data: EEGArtifactMetricCompressedDto) : TypedMetric()
    data class MEMSCompressed(val data: MemsMetricCompressedDto) : TypedMetric()
    data class ProductivityCompressed(val data: ProductivityMetricCompressedDto) : TypedMetric()
    data class EmotionalCompressed(val data: EmotionalMetricCompressedDto) : TypedMetric()
    data class CardioCompressed(val data: CardioMetricCompressedDto) : TypedMetric()

    data class PhysiologicalBaseline(val data: PhysiologicalBaselineDto) : TypedMetric()
    data class ProductivityBaseline(val data: ProductivityBaselineDto) : TypedMetric()
    data class ProductivityIndex(val data: ProductivityIndexDto) : TypedMetric()
    data class Session(val data: SessionDto) : TypedMetric()
}

/**
 * Пакет метрик для отправки
 */
data class MetricBatch(
    val request: UploadRequest,
    val recordCount: Int,
    val batchId: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Результат отправки пакета
 */
sealed class BatchSendResult {
    data class Success(val batchId: String) : BatchSendResult()
    data class Failure(val batchId: String, val message: String) : BatchSendResult()
}

/**
 * Прогресс пакетной отправки
 */
sealed class BatchUploadProgress {
    data class Preparing(val step: String, val progress: Float) : BatchUploadProgress()
    data class BatchesCreated(val totalBatches: Int, val totalRecords: Int) : BatchUploadProgress()
    data class SendingBatch(
        val current: Int,
        val total: Int,
        val recordsInBatch: Int,
        val batchId: String
    ) : BatchUploadProgress()

    data class BatchCompleted(
        val batchIndex: Int,
        val totalBatches: Int,
        val recordsInBatch: Int,
        val totalSentSoFar: Int,
        val batchId: String
    ) : BatchUploadProgress()

    data class BatchFailed(
        val batchIndex: Int,
        val totalBatches: Int,
        val error: String,
        val batchId: String,
        val willRetry: Boolean = false
    ) : BatchUploadProgress()

    data class Stopped(
        val sentCount: Int,
        val failedCount: Int,
        val reason: String
    ) : BatchUploadProgress()

    data class PartialSuccess(
        val sentCount: Int,
        val totalCount: Int,
        val failedBatches: Int,
        val failedRecords: Int
    ) : BatchUploadProgress()

    data class Completed(
        val sentCount: Int,
        val totalBatches: Int
    ) : BatchUploadProgress()

    object NoData : BatchUploadProgress()
    data class Error(val message: String) : BatchUploadProgress()
}

/**
 * Расширение для проверки наличия данных в запросе
 */
fun UploadRequest.hasData(): Boolean {
    return (nfbMetrics?.isNotEmpty() == true) ||
            (physiologicalMetrics?.isNotEmpty() == true) ||
            (EEGRawMetrics?.isNotEmpty() == true) ||
            (EEGProceedMetrics?.isNotEmpty() == true) ||
            (EEGArtifactsMetrics?.isNotEmpty() == true) ||
            (memsMetrics?.isNotEmpty() == true) ||
            (productivityMetrics?.isNotEmpty() == true) ||
            (emotionalMetrics?.isNotEmpty() == true) ||
            (cardioMetrics?.isNotEmpty() == true) ||
            (nfbMetricsCompressed?.isNotEmpty() == true) ||
            (physiologicalMetricsCompressed?.isNotEmpty() == true) ||
            (EEGRawMetricsCompressed?.isNotEmpty() == true) ||
            (EEGProceedMetricsCompressed?.isNotEmpty() == true) ||
            (EEGArtifactsMetricsCompressed?.isNotEmpty() == true) ||
            (memsMetricsCompressed?.isNotEmpty() == true) ||
            (productivityMetricsCompressed?.isNotEmpty() == true) ||
            (emotionalMetricsCompressed?.isNotEmpty() == true) ||
            (cardioMetricsCompressed?.isNotEmpty() == true) ||
            (physiologicalBaseline?.isNotEmpty() == true) ||
            (productivityBaseline?.isNotEmpty() == true) ||
            (productivityIndex?.isNotEmpty() == true) ||
            (sessionResult?.isNotEmpty() == true)
}

// ==================== СУЩЕСТВУЮЩИЕ КЛАССЫ (ОСТАВЛЯЕМ БЕЗ ИЗМЕНЕНИЙ) ====================

sealed class UploadPreparationResult {
    object NoData : UploadPreparationResult()
    data class Ready(
        val request: UploadRequest,
        val totalRecords: Int,
        val nfbCount: Int,
        val physiologicalCount: Int,
        val eegRawCount: Int,
        val eegProceedCount: Int,
        val eegArtifactsCount: Int,
        val memsCount: Int,
        val productivityCount: Int,
        val emotionalCount: Int,
        val cardioCount: Int,
        val nfbCompressedCount: Int,
        val physiologicalCompressedCount: Int,
        val eegRawCompressedCount: Int,
        val eegProceedCompressedCount: Int,
        val eegArtifactsCompressedCount: Int,
        val memsCompressedCount: Int,
        val productivityCompressedCount: Int,
        val emotionalCompressedCount: Int,
        val cardioCompressedCount: Int,
        val physiologicalBaselinesCount: Int,
        val productivityBaselinesCount: Int,
        val productivityIndexesCount: Int,
        val sessionsCount: Int
    ) : UploadPreparationResult()
    data class Error(val message: String) : UploadPreparationResult()
}

sealed class UploadResult {
    object NoData : UploadResult()
    data class Success(val sentCount: Int, val message: String = "Данные загружены") : UploadResult()
    data class Error(val message: String) : UploadResult()
}

sealed class UploadProgress {
    data class Preparing(val step: String, val progress: Float) : UploadProgress()
    data class Completed(val sentCount: Int, val message: String) : UploadProgress()
    object NoData : UploadProgress()
    data class Error(val message: String) : UploadProgress()
}

sealed class FileSaveResult {
    object NoData : FileSaveResult()
    data class Success(val filePath: String, val fileName: String, val recordsCount: Int) : FileSaveResult()
    data class Error(val message: String) : FileSaveResult()
}

data class UploadStats(
    val totalRecords: Int,
    val unsyncedRecords: Int,
    val nfbCount: Int,
    val physiologicalCount: Int,
    val eegRawCount: Int,
    val eegProceedCount: Int,
    val eegArtifactsCount: Int,
    val memsCount: Int,
    val productivityCount: Int,
    val emotionalCount: Int,
    val cardioCount: Int,
    val nfbCompressedCount: Int,
    val physiologicalCompressedCount: Int,
    val eegRawCompressedCount: Int,
    val eegProceedCompressedCount: Int,
    val eegArtifactsCompressedCount: Int,
    val memsCompressedCount: Int,
    val productivityCompressedCount: Int,
    val emotionalCompressedCount: Int,
    val cardioCompressedCount: Int,
    val physiologicalBaselinesCount: Int,
    val productivityBaselinesCount: Int,
    val productivityIndexesCount: Int,
    val sessionsCount: Int,
    val nfbUnsynced: Int,
    val physiologicalUnsynced: Int,
    val eegRawUnsynced: Int,
    val eegProceedUnsynced: Int,
    val eegArtifactsUnsynced: Int,
    val memsUnsynced: Int,
    val productivityUnsynced: Int,
    val emotionalUnsynced: Int,
    val cardioUnsynced: Int,
    val nfbCompressedUnsynced: Int,
    val physiologicalCompressedUnsynced: Int,
    val eegRawCompressedUnsynced: Int,
    val eegProceedCompressedUnsynced: Int,
    val eegArtifactsCompressedUnsynced: Int,
    val memsCompressedUnsynced: Int,
    val productivityCompressedUnsynced: Int,
    val emotionalCompressedUnsynced: Int,
    val cardioCompressedUnsynced: Int,
    val physiologicalBaselinesUnsynced: Int,
    val productivityBaselinesUnsynced: Int,
    val productivityIndexesUnsynced: Int,
    val sessionsUnsynced: Int
)
