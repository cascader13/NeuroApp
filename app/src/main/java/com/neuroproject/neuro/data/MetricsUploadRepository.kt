// [file name]: MetricsUploadRepository.kt
package com.neuroproject.neuro.data

import android.content.Context
import com.google.gson.Gson
import com.neuroproject.neuro.data.*
import com.neuroproject.neuro.data.remote.EEGArtifactMetricDto
import com.neuroproject.neuro.data.remote.EEGProceedMetricDto
import com.neuroproject.neuro.data.remote.MetricsApiService
import com.neuroproject.neuro.data.remote.UploadRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetricsUploadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val metricsDao: MetricsDao,
    private val gson: Gson,
    private val apiService: MetricsApiService // Retrofit сервис
) {

    /**
     * Подготовить данные для отправки
     */
    suspend fun prepareDataForUpload(): UploadPreparationResult {
        return try {
            val nfbMetrics = metricsDao.getUnmarkedNFBMetrics()
            val physiologicalMetrics = metricsDao.getUnmarkedPhysiologicalMetrics()
            val memsMetrics = metricsDao.getUnmarkedMEMSMetrics()
            val productivityMetrics = metricsDao.getUnmarkedProductivityMetrics()
            val emotionalMetrics = metricsDao.getUnmarkedEmotionalMetrics()
            val cardioMetrics = metricsDao.getUnmarkedCardioMetrics()
            val EEGRawMetrics = metricsDao.getUnmarkedEEGRAWMetrics()
            val EEGProceedMetrics = metricsDao.getUnmarkedEEGPROCEEDMetrics()
            val EEGArtifactMetrics = metricsDao.getUnmarkedEEGArtifactsMetrics()

            val totalRecords = nfbMetrics.size + physiologicalMetrics.size + memsMetrics.size +
                    productivityMetrics.size + emotionalMetrics.size + cardioMetrics.size

            if (totalRecords == 0) {
                return UploadPreparationResult.NoData
            }

            // Создаем DTO для отправки
            val uploadRequest = UploadRequest(
                cardioMetrics = cardioMetrics.map { it.toServerDto() },
                emotionalMetrics = emotionalMetrics.map { it.toServerDto() },
                memsMetrics = memsMetrics.map { it.toServerDto() },
                nfbMetrics = nfbMetrics.map { it.toServerDto() },
                physiologicalMetrics = physiologicalMetrics.map { it.toServerDto() },
                productivityMetrics = productivityMetrics.map { it.toServerDto() },
                EEGRawMetrics = EEGRawMetrics.map {it.toServerDto()},
                EEGProceedMetrics = EEGProceedMetrics.map {it.toServerDto()},
                EEGArtifactsMetrics = EEGArtifactMetrics.map{it.toServerDto()}

            )

            UploadPreparationResult.Ready(
                request = uploadRequest,
                totalRecords = totalRecords,
                nfbCount = nfbMetrics.size,
                physiologicalCount = physiologicalMetrics.size,
                memsCount = memsMetrics.size,
                productivityCount = productivityMetrics.size,
                emotionalCount = emotionalMetrics.size,
                cardioCount = cardioMetrics.size,
                EEGRAWCount = EEGRawMetrics.size,
                EEGPROCEEDCount = EEGProceedMetrics.size,
                EEGArtifactsCount = EEGArtifactMetrics.size
            )

        } catch (e: Exception) {
            UploadPreparationResult.Error("Ошибка подготовки данных: ${e.message}")
        }
    }

    /**
     * Отправить данные на сервер
     */
    suspend fun uploadToServer(): UploadResult {
        return try {
            // 1. Подготовить данные
            val preparationResult = prepareDataForUpload()

            when (preparationResult) {
                is UploadPreparationResult.NoData -> {
                    return UploadResult.NoData
                }
                is UploadPreparationResult.Error -> {
                    return UploadResult.Error(preparationResult.message)
                }
                is UploadPreparationResult.Ready -> {
                    // 2. Отправить на сервер
                    val response = apiService.uploadMetrics(preparationResult.request)

                    if (response.isSuccessful) {
                        val responseBody = response.body()

                        if (/*responseBody?.result == true*/ response.isSuccessful) {
                            // 3. Пометить данные как отправленные
                            markDataAsUploaded()

                            UploadResult.Success(
                                sentCount = preparationResult.totalRecords
                            )
                        } else {
                            UploadResult.Error("Неизвестная ошибка сервера")
                        }
                    } else {
                        // Обработка HTTP ошибок
                        val errorMessage = when (response.code()) {
                            400 -> "Некорректный запрос"
                            401 -> "Требуется авторизация"
                            403 -> "Доступ запрещен"
                            404 -> "Сервер не найден"
                            500 -> "Внутренняя ошибка сервера"
                            else -> "HTTP ${response.code()}: ${response.message()}"
                        }
                        UploadResult.Error(errorMessage)
                    }
                }
            }

        } catch (e: HttpException) {
            UploadResult.Error("HTTP ошибка: ${e.message}")
        } catch (e: Exception) {
            UploadResult.Error("Ошибка сети: ${e.message}")
        }
    }

    /**
     * Потоковая отправка с прогрессом
     */
    fun uploadWithProgress(): Flow<UploadProgress> = flow {
        try {
            emit(UploadProgress.Preparing("Подготовка данных...", 0.1f))

            val preparationResult = prepareDataForUpload()

            when (preparationResult) {
                is UploadPreparationResult.NoData -> {
                    emit(UploadProgress.NoData)
                }
                is UploadPreparationResult.Error -> {
                    emit(UploadProgress.Error(preparationResult.message))
                }
                is UploadPreparationResult.Ready -> {
                    emit(UploadProgress.Preparing("Отправка на сервер...", 0.5f))

                    // Отправляем данные
                    val result = uploadToServer()

                    when (result) {
                        is UploadResult.Success -> {
                            emit(UploadProgress.Completed(result.sentCount, result.message))
                        }
                        is UploadResult.Error -> {
                            emit(UploadProgress.Error(result.message))
                        }
                        UploadResult.NoData -> {
                            emit(UploadProgress.NoData)
                        }
                    }
                }
            }

        } catch (e: Exception) {
            emit(UploadProgress.Error("Неизвестная ошибка: ${e.message}"))
        }
    }

    /**
     * Сохранить данные в JSON файл (для отладки)
     */
    suspend fun saveToJsonFile(): FileSaveResult {
        return try {
            val preparationResult = prepareDataForUpload()

            when (preparationResult) {
                is UploadPreparationResult.NoData -> {
                    FileSaveResult.NoData
                }
                is UploadPreparationResult.Error -> {
                    FileSaveResult.Error(preparationResult.message)
                }
                is UploadPreparationResult.Ready -> {
                    val exportDir = File(context.filesDir, "metrics_exports")
                    if (!exportDir.exists()) {
                        exportDir.mkdirs()
                    }

                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
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

        } catch (e: Exception) {
            FileSaveResult.Error("Ошибка сохранения файла: ${e.message}")
        }
    }

    private suspend fun markDataAsUploaded() {
        // Помечаем все непомеченные данные как отправленные
        val nfbTimestamps = metricsDao.getUnmarkedNFBMetrics().map { it.timestamp }
        val physioTimestamps = metricsDao.getUnmarkedPhysiologicalMetrics().map { it.timestamp }
        val memsTimestamps = metricsDao.getUnmarkedMEMSMetrics().map { it.timestamp }
        val prodTimestamps = metricsDao.getUnmarkedProductivityMetrics().map { it.timestamp }
        val emotTimestamps = metricsDao.getUnmarkedEmotionalMetrics().map { it.timestamp }
        val cardioTimestamps = metricsDao.getUnmarkedCardioMetrics().map { it.timestamp }
        val EEGRAWTimestamps = metricsDao.getUnmarkedEEGRAWMetrics().map {it.timestamp}
        val EEGProceedTimestamps = metricsDao.getUnmarkedEEGPROCEEDMetrics().map {it.timestamp}
        val EEGArtifactsTimestamps = metricsDao.getUnmarkedEEGArtifactsMetrics().map {it.timestamp}


        if (nfbTimestamps.isNotEmpty()) metricsDao.markNFBMetricsAsSynced(nfbTimestamps)
        if (physioTimestamps.isNotEmpty()) metricsDao.markPhysiologicalMetricsAsSynced(physioTimestamps)
        if (memsTimestamps.isNotEmpty()) metricsDao.markMEMSMetricsAsSynced(memsTimestamps)
        if (prodTimestamps.isNotEmpty()) metricsDao.markProductivityMetricsAsSynced(prodTimestamps)
        if (emotTimestamps.isNotEmpty()) metricsDao.markEmotionalMetricsAsSynced(emotTimestamps)
        if (cardioTimestamps.isNotEmpty()) metricsDao.markCardioMetricsAsSynced(cardioTimestamps)
        if (EEGRAWTimestamps.isNotEmpty()) metricsDao.markEEGRAWMetricsAsSynced(EEGRAWTimestamps)
        if (EEGProceedTimestamps.isNotEmpty()) metricsDao.markEEGProceedMetricsAsSynced(EEGProceedTimestamps)
        if (EEGArtifactsTimestamps.isNotEmpty()) metricsDao.markEEGArtifactsMetricsAsSynced(EEGArtifactsTimestamps)
    }

    /**
     * Получить статистику
     */
    suspend fun getStats(): UploadStats {
        val totalRecords = metricsDao.getAllNFBMetricsCount() +
                metricsDao.getAllPhysiologicalMetricsCount() +
                metricsDao.getAllMEMSMetricsCount() +
                metricsDao.getAllProductivityMetricsCount() +
                metricsDao.getAllEmotionalMetricsCount() +
                metricsDao.getAllCardioMetricsCount() +
                metricsDao.getAllEEGRAWMetricsCount() +
                metricsDao.getAllEEGPROCEEDMetricsCount() +
                metricsDao.getAllEEGArtifactsMetricsCount()

        val unsyncedRecords = metricsDao.getUnmarkedNFBMetricsCount() +
                metricsDao.getUnmarkedPhysiologicalMetricsCount() +
                metricsDao.getUnmarkedMEMSMetricsCount() +
                metricsDao.getUnmarkedProductivityMetricsCount() +
                metricsDao.getUnmarkedEmotionalMetricsCount() +
                metricsDao.getUnmarkedCardioMetricsCount() +
                metricsDao.getUnmarkedEEGRAWMetricsCount() +
                metricsDao.getUnmarkedEEGPROCEEDMetricsCount() +
                metricsDao.getUnmarkedEEGArtifactMetricsCount()

        return UploadStats(
            totalRecords = totalRecords,
            unsyncedRecords = unsyncedRecords,
            nfbCount = metricsDao.getAllNFBMetricsCount(),
            physiologicalCount = metricsDao.getAllPhysiologicalMetricsCount(),
            memsCount = metricsDao.getAllMEMSMetricsCount(),
            productivityCount = metricsDao.getAllProductivityMetricsCount(),
            emotionalCount = metricsDao.getAllEmotionalMetricsCount(),
            cardioCount = metricsDao.getAllCardioMetricsCount(),
            EEGRAWCount = metricsDao.getAllEEGRAWMetricsCount(),
            EEGPROCEEDCount = metricsDao.getAllEEGPROCEEDMetricsCount(),
            EEGArtifactsCount = metricsDao.getAllEEGArtifactsMetricsCount(),
            nfbUnsynced = metricsDao.getUnmarkedNFBMetricsCount(),
            physiologicalUnsynced = metricsDao.getUnmarkedPhysiologicalMetricsCount(),
            memsUnsynced = metricsDao.getUnmarkedMEMSMetricsCount(),
            productivityUnsynced = metricsDao.getUnmarkedProductivityMetricsCount(),
            emotionalUnsynced = metricsDao.getUnmarkedEmotionalMetricsCount(),
            cardioUnsynced = metricsDao.getUnmarkedCardioMetricsCount(),
            EEGRAWUnsynced = metricsDao.getUnmarkedEEGRAWMetricsCount(),
            EEGPROCEEDUnsynced = metricsDao.getUnmarkedEEGPROCEEDMetricsCount(),
            EEGArtifactsUnsynced = metricsDao.getUnmarkedEEGArtifactMetricsCount()
        )
    }
}

// Классы данных для подготовки
sealed class UploadPreparationResult {
    object NoData : UploadPreparationResult()
    data class Ready(
        val request: UploadRequest,
        val totalRecords: Int,
        val nfbCount: Int,
        val physiologicalCount: Int,
        val memsCount: Int,
        val productivityCount: Int,
        val emotionalCount: Int,
        val cardioCount: Int,
        val EEGRAWCount: Int,
        val EEGPROCEEDCount: Int,
        val EEGArtifactsCount: Int
    ) : UploadPreparationResult()
    data class Error(val message: String) : UploadPreparationResult()
}

// Результаты отправки
sealed class UploadResult {
    object NoData : UploadResult()
    data class Success(val sentCount: Int, val message: String = "Данные загружены") : UploadResult()
    data class Error(val message: String) : UploadResult()
}

// Прогресс отправки
sealed class UploadProgress {
    data class Preparing(val step: String, val progress: Float) : UploadProgress()
    data class Completed(val sentCount: Int, val message: String) : UploadProgress()
    object NoData : UploadProgress()
    data class Error(val message: String) : UploadProgress()
}

// Результаты сохранения файла
sealed class FileSaveResult {
    object NoData : FileSaveResult()
    data class Success(val filePath: String, val fileName: String, val recordsCount: Int) : FileSaveResult()
    data class Error(val message: String) : FileSaveResult()
}

// Статистика
data class UploadStats(
    val totalRecords: Int,
    val unsyncedRecords: Int,
    val nfbCount: Int,
    val physiologicalCount: Int,
    val memsCount: Int,
    val productivityCount: Int,
    val emotionalCount: Int,
    val cardioCount: Int,
    val EEGRAWCount: Int,
    val EEGPROCEEDCount: Int,
    val EEGArtifactsCount: Int,
    val nfbUnsynced: Int,
    val physiologicalUnsynced: Int,
    val memsUnsynced: Int,
    val productivityUnsynced: Int,
    val emotionalUnsynced: Int,
    val cardioUnsynced: Int,
    val EEGRAWUnsynced: Int,
    val EEGPROCEEDUnsynced: Int,
    val EEGArtifactsUnsynced: Int

)