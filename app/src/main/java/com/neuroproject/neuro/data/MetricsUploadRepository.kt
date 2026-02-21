package com.neuroproject.neuro.data

import android.content.Context
import com.google.gson.Gson
import com.neuroproject.neuro.data.remote.*
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
    private val apiService: MetricsApiService
) {

    /**
     * Подготовить данные для отправки (включая compressed таблицы)
     */
    suspend fun prepareDataForUpload(): UploadPreparationResult {
        return try {
            // Uncompressed данные
            val nfbMetrics = metricsDao.getUnmarkedNFBMetrics()
            val physiologicalMetrics = metricsDao.getUnmarkedPhysiologicalMetrics()
            val memsMetrics = metricsDao.getUnmarkedMEMSMetrics()
            val productivityMetrics = metricsDao.getUnmarkedProductivityMetrics()
            val emotionalMetrics = metricsDao.getUnmarkedEmotionalMetrics()
            val cardioMetrics = metricsDao.getUnmarkedCardioMetrics()
            val EEGRawMetrics = metricsDao.getUnmarkedEEGRAWMetrics()
            val EEGProceedMetrics = metricsDao.getUnmarkedEEGPROCEEDMetrics()
            val EEGArtifactMetrics = metricsDao.getUnmarkedEEGArtifactsMetrics()

            // Compressed данные
            val nfbMetricsCompressed = metricsDao.getUnmarkedNFBMetricsCompressed()
            val physiologicalMetricsCompressed = metricsDao.getUnmarkedPhysiologicalMetricsCompressed()
            val memsMetricsCompressed = metricsDao.getUnmarkedMEMSMetricsCompressed()
            val productivityMetricsCompressed = metricsDao.getUnmarkedProductivityMetricsCompressed()
            val emotionalMetricsCompressed = metricsDao.getUnmarkedEmotionalMetricsCompressed()
            val cardioMetricsCompressed = metricsDao.getUnmarkedCardioMetricsCompressed()
            val EEGRawMetricsCompressed = metricsDao.getUnmarkedEEGRAWMetricsCompressed()
            val EEGProceedMetricsCompressed = metricsDao.getUnmarkedEEGPROCEEDMetricsCompressed()
            val EEGArtifactMetricsCompressed = metricsDao.getUnmarkedEEGArtifactsMetricsCompressed()

            val totalRecords =
                nfbMetrics.size + physiologicalMetrics.size + memsMetrics.size +
                        productivityMetrics.size + emotionalMetrics.size + cardioMetrics.size +
                        EEGArtifactMetrics.size + EEGProceedMetrics.size + EEGRawMetrics.size +
                        nfbMetricsCompressed.size + physiologicalMetricsCompressed.size + memsMetricsCompressed.size +
                        productivityMetricsCompressed.size + emotionalMetricsCompressed.size + cardioMetricsCompressed.size +
                        EEGArtifactMetricsCompressed.size + EEGProceedMetricsCompressed.size + EEGRawMetricsCompressed.size

            if (totalRecords == 0) {
                return UploadPreparationResult.NoData
            }

            // Создаем DTO для отправки
            val uploadRequest = UploadRequest(
                // Uncompressed данные
                cardioMetrics = cardioMetrics.map { it.toServerDto() },
                emotionalMetrics = emotionalMetrics.map { it.toServerDto() },
                memsMetrics = memsMetrics.map { it.toServerDto() },
                nfbMetrics = nfbMetrics.map { it.toServerDto() },
                physiologicalMetrics = physiologicalMetrics.map { it.toServerDto() },
                productivityMetrics = productivityMetrics.map { it.toServerDto() },
                EEGRawMetrics = EEGRawMetrics.map { it.toServerDto() },
                EEGProceedMetrics = EEGProceedMetrics.map { it.toServerDto() },
                EEGArtifactsMetrics = EEGArtifactMetrics.map { it.toServerDto() },

                // Compressed данные
                nfbMetricsCompressed = nfbMetricsCompressed.map { it.toServerDto() },
                physiologicalMetricsCompressed = physiologicalMetricsCompressed.map { it.toServerDto() },
                memsMetricsCompressed = memsMetricsCompressed.map { it.toServerDto() },
                productivityMetricsCompressed = productivityMetricsCompressed.map { it.toServerDto() },
                emotionalMetricsCompressed = emotionalMetricsCompressed.map { it.toServerDto() },
                cardioMetricsCompressed = cardioMetricsCompressed.map { it.toServerDto() },
                EEGRawMetricsCompressed = EEGRawMetricsCompressed.map { it.toServerDto() },
                EEGProceedMetricsCompressed = EEGProceedMetricsCompressed.map { it.toServerDto() },
                EEGArtifactsMetricsCompressed = EEGArtifactMetricsCompressed.map { it.toServerDto() }
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
                EEGArtifactsCount = EEGArtifactMetrics.size,
                nfbCompressedCount = nfbMetricsCompressed.size,
                physiologicalCompressedCount = physiologicalMetricsCompressed.size,
                memsCompressedCount = memsMetricsCompressed.size,
                productivityCompressedCount = productivityMetricsCompressed.size,
                emotionalCompressedCount = emotionalMetricsCompressed.size,
                cardioCompressedCount = cardioMetricsCompressed.size,
                EEGRAWCompressedCount = EEGRawMetricsCompressed.size,
                EEGPROCEEDCompressedCount = EEGProceedMetricsCompressed.size,
                EEGArtifactsCompressedCount = EEGArtifactMetricsCompressed.size
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
            val preparationResult = prepareDataForUpload()

            when (preparationResult) {
                is UploadPreparationResult.NoData -> {
                    return UploadResult.NoData
                }
                is UploadPreparationResult.Error -> {
                    return UploadResult.Error(preparationResult.message)
                }
                is UploadPreparationResult.Ready -> {
                    val response = apiService.uploadMetrics(preparationResult.request)

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (response.isSuccessful) {
                            markDataAsUploaded()
                            UploadResult.Success(
                                sentCount = preparationResult.totalRecords
                            )
                        } else {
                            UploadResult.Error("Неизвестная ошибка сервера")
                        }
                    } else {
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
        val preparationResult = prepareDataForUpload()

        when (preparationResult) {
            is UploadPreparationResult.Ready -> {
                // Uncompressed timestamps
                val nfbTimestamps = preparationResult.request.nfbMetrics?.map { it.timestamp } ?: emptyList()
                val physioTimestamps = preparationResult.request.physiologicalMetrics?.map { it.timestamp } ?: emptyList()
                val memsTimestamps = preparationResult.request.memsMetrics?.map { it.timestamp } ?: emptyList()
                val prodTimestamps = preparationResult.request.productivityMetrics?.map { it.timestamp } ?: emptyList()
                val emotTimestamps = preparationResult.request.emotionalMetrics?.map { it.timestamp } ?: emptyList()
                val cardioTimestamps = preparationResult.request.cardioMetrics?.map { it.timestamp } ?: emptyList()
                val EEGRAWTimestamps = preparationResult.request.EEGRawMetrics?.map { it.timestamp } ?: emptyList()
                val EEGProceedTimestamps = preparationResult.request.EEGProceedMetrics?.map { it.timestamp } ?: emptyList()
                val EEGArtifactsTimestamps = preparationResult.request.EEGArtifactsMetrics?.map { it.timestamp } ?: emptyList()

                // Compressed timestamps
                val nfbCompressedTimestamps = preparationResult.request.nfbMetricsCompressed?.map { it.timestamp } ?: emptyList()
                val physioCompressedTimestamps = preparationResult.request.physiologicalMetricsCompressed?.map { it.timestamp } ?: emptyList()
                val memsCompressedTimestamps = preparationResult.request.memsMetricsCompressed?.map { it.timestamp } ?: emptyList()
                val prodCompressedTimestamps = preparationResult.request.productivityMetricsCompressed?.map { it.timestamp } ?: emptyList()
                val emotCompressedTimestamps = preparationResult.request.emotionalMetricsCompressed?.map { it.timestamp } ?: emptyList()
                val cardioCompressedTimestamps = preparationResult.request.cardioMetricsCompressed?.map { it.timestamp } ?: emptyList()
                val EEGRAWCompressedTimestamps = preparationResult.request.EEGRawMetricsCompressed?.map { it.timestamp } ?: emptyList()
                val EEGProceedCompressedTimestamps = preparationResult.request.EEGProceedMetricsCompressed?.map { it.timestamp } ?: emptyList()
                val EEGArtifactsCompressedTimestamps = preparationResult.request.EEGArtifactsMetricsCompressed?.map { it.timestamp } ?: emptyList()

                // Помечаем uncompressed данные
                if (nfbTimestamps.isNotEmpty()) metricsDao.safeMarkNFBMetricsAsSynced(nfbTimestamps)
                if (physioTimestamps.isNotEmpty()) metricsDao.safeMarkPhysiologicalMetricsAsSynced(physioTimestamps)
                if (memsTimestamps.isNotEmpty()) metricsDao.safeMarkMEMSMetricsAsSynced(memsTimestamps)
                if (prodTimestamps.isNotEmpty()) metricsDao.safeMarkProductivityMetricsAsSynced(prodTimestamps)
                if (emotTimestamps.isNotEmpty()) metricsDao.safeMarkEmotionalMetricsAsSynced(emotTimestamps)
                if (cardioTimestamps.isNotEmpty()) metricsDao.safeMarkCardioMetricsAsSynced(cardioTimestamps)
                if (EEGRAWTimestamps.isNotEmpty()) metricsDao.safeMarkEEGRAWMetricsAsSynced(EEGRAWTimestamps)
                if (EEGProceedTimestamps.isNotEmpty()) metricsDao.safeMarkEEGProceedMetricsAsSynced(EEGProceedTimestamps)
                if (EEGArtifactsTimestamps.isNotEmpty()) metricsDao.safeMarkEEGArtifactsMetricsAsSynced(EEGArtifactsTimestamps)

                // Помечаем compressed данные
                if (nfbCompressedTimestamps.isNotEmpty()) metricsDao.safeMarkNFBMetricsCompressedAsSynced(nfbCompressedTimestamps)
                if (physioCompressedTimestamps.isNotEmpty()) metricsDao.safeMarkPhysiologicalMetricsCompressedAsSynced(physioCompressedTimestamps)
                if (memsCompressedTimestamps.isNotEmpty()) metricsDao.safeMarkMEMSMetricsCompressedAsSynced(memsCompressedTimestamps)
                if (prodCompressedTimestamps.isNotEmpty()) metricsDao.safeMarkProductivityMetricsCompressedAsSynced(prodCompressedTimestamps)
                if (emotCompressedTimestamps.isNotEmpty()) metricsDao.safeMarkEmotionalMetricsCompressedAsSynced(emotCompressedTimestamps)
                if (cardioCompressedTimestamps.isNotEmpty()) metricsDao.safeMarkCardioMetricsCompressedAsSynced(cardioCompressedTimestamps)
                if (EEGRAWCompressedTimestamps.isNotEmpty()) metricsDao.safeMarkEEGRAWMetricsCompressedAsSynced(EEGRAWCompressedTimestamps)
                if (EEGProceedCompressedTimestamps.isNotEmpty()) metricsDao.safeMarkEEGProceedMetricsAsSynced(EEGProceedCompressedTimestamps)
                if (EEGArtifactsCompressedTimestamps.isNotEmpty()) metricsDao.safeMarkEEGArtifactsMetricsCompressedAsSynced(EEGArtifactsCompressedTimestamps)
            }
            else -> { /* Нет данных для пометки */ }
        }
    }

    /**
     * Получить статистику
     */
    suspend fun getStats(): UploadStats {
        // Uncompressed counts
        val nfbCount = metricsDao.getAllNFBMetricsCount()
        val physiologicalCount = metricsDao.getAllPhysiologicalMetricsCount()
        val memsCount = metricsDao.getAllMEMSMetricsCount()
        val productivityCount = metricsDao.getAllProductivityMetricsCount()
        val emotionalCount = metricsDao.getAllEmotionalMetricsCount()
        val cardioCount = metricsDao.getAllCardioMetricsCount()
        val EEGRAWCount = metricsDao.getAllEEGRAWMetricsCount()
        val EEGPROCEEDCount = metricsDao.getAllEEGPROCEEDMetricsCount()
        val EEGArtifactsCount = metricsDao.getAllEEGArtifactsMetricsCount()

        // Compressed counts
        val nfbCompressedCount = metricsDao.getAllNFBMetricsCountCompressed()
        val physiologicalCompressedCount = metricsDao.getAllPhysiologicalMetricsCountCompressed()
        val memsCompressedCount = metricsDao.getAllMEMSMetricsCountCompressed()
        val productivityCompressedCount = metricsDao.getAllProductivityMetricsCountCompressed()
        val emotionalCompressedCount = metricsDao.getAllEmotionalMetricsCountCompressed()
        val cardioCompressedCount = metricsDao.getAllCardioMetricsCountCompressed()
        val EEGRAWCompressedCount = metricsDao.getAllEEGRAWMetricsCountCompressed()
        val EEGPROCEEDCompressedCount = metricsDao.getAllEEGPROCEEDMetricsCountCompressed()
        val EEGArtifactsCompressedCount = metricsDao.getAllEEGArtifactsMetricsCountCompressed()

        // Uncompressed unsynced counts
        val nfbUnsynced = metricsDao.getUnmarkedNFBMetricsCount()
        val physiologicalUnsynced = metricsDao.getUnmarkedPhysiologicalMetricsCount()
        val memsUnsynced = metricsDao.getUnmarkedMEMSMetricsCount()
        val productivityUnsynced = metricsDao.getUnmarkedProductivityMetricsCount()
        val emotionalUnsynced = metricsDao.getUnmarkedEmotionalMetricsCount()
        val cardioUnsynced = metricsDao.getUnmarkedCardioMetricsCount()
        val EEGRAWUnsynced = metricsDao.getUnmarkedEEGRAWMetricsCount()
        val EEGPROCEEDUnsynced = metricsDao.getUnmarkedEEGPROCEEDMetricsCount()
        val EEGArtifactsUnsynced = metricsDao.getUnmarkedEEGArtifactMetricsCount()

        // Compressed unsynced counts
        val nfbCompressedUnsynced = metricsDao.getUnmarkedNFBMetricsCountCompressed()
        val physiologicalCompressedUnsynced = metricsDao.getUnmarkedPhysiologicalMetricsCountCompressed()
        val memsCompressedUnsynced = metricsDao.getUnmarkedMEMSMetricsCountCompressed()
        val productivityCompressedUnsynced = metricsDao.getUnmarkedProductivityMetricsCountCompressed()
        val emotionalCompressedUnsynced = metricsDao.getUnmarkedEmotionalMetricsCountCompressed()
        val cardioCompressedUnsynced = metricsDao.getUnmarkedCardioMetricsCountCompressed()
        val EEGRAWCompressedUnsynced = metricsDao.getUnmarkedEEGRAWMetricsCountCompressed()
        val EEGPROCEEDCompressedUnsynced = metricsDao.getUnmarkedEEGPROCEEDMetricsCountCompressed()
        val EEGArtifactsCompressedUnsynced = metricsDao.getUnmarkedEEGArtifactMetricsCountCompressed()

        val totalRecords = nfbCount + physiologicalCount + memsCount + productivityCount +
                emotionalCount + cardioCount + EEGRAWCount + EEGPROCEEDCount + EEGArtifactsCount +
                nfbCompressedCount + physiologicalCompressedCount + memsCompressedCount +
                productivityCompressedCount + emotionalCompressedCount + cardioCompressedCount +
                EEGRAWCompressedCount + EEGPROCEEDCompressedCount + EEGArtifactsCompressedCount

        val unsyncedRecords = nfbUnsynced + physiologicalUnsynced + memsUnsynced + productivityUnsynced +
                emotionalUnsynced + cardioUnsynced + EEGRAWUnsynced + EEGPROCEEDUnsynced + EEGArtifactsUnsynced +
                nfbCompressedUnsynced + physiologicalCompressedUnsynced + memsCompressedUnsynced +
                productivityCompressedUnsynced + emotionalCompressedUnsynced + cardioCompressedUnsynced +
                EEGRAWCompressedUnsynced + EEGPROCEEDCompressedUnsynced + EEGArtifactsCompressedUnsynced

        return UploadStats(
            totalRecords = totalRecords,
            unsyncedRecords = unsyncedRecords,
            nfbCount = nfbCount,
            physiologicalCount = physiologicalCount,
            memsCount = memsCount,
            productivityCount = productivityCount,
            emotionalCount = emotionalCount,
            cardioCount = cardioCount,
            EEGRAWCount = EEGRAWCount,
            EEGPROCEEDCount = EEGPROCEEDCount,
            EEGArtifactsCount = EEGArtifactsCount,
            nfbCompressedCount = nfbCompressedCount,
            physiologicalCompressedCount = physiologicalCompressedCount,
            memsCompressedCount = memsCompressedCount,
            productivityCompressedCount = productivityCompressedCount,
            emotionalCompressedCount = emotionalCompressedCount,
            cardioCompressedCount = cardioCompressedCount,
            EEGRAWCompressedCount = EEGRAWCompressedCount,
            EEGPROCEEDCompressedCount = EEGPROCEEDCompressedCount,
            EEGArtifactsCompressedCount = EEGArtifactsCompressedCount,
            nfbUnsynced = nfbUnsynced,
            physiologicalUnsynced = physiologicalUnsynced,
            memsUnsynced = memsUnsynced,
            productivityUnsynced = productivityUnsynced,
            emotionalUnsynced = emotionalUnsynced,
            cardioUnsynced = cardioUnsynced,
            EEGRAWUnsynced = EEGRAWUnsynced,
            EEGPROCEEDUnsynced = EEGPROCEEDUnsynced,
            EEGArtifactsUnsynced = EEGArtifactsUnsynced,
            nfbCompressedUnsynced = nfbCompressedUnsynced,
            physiologicalCompressedUnsynced = physiologicalCompressedUnsynced,
            memsCompressedUnsynced = memsCompressedUnsynced,
            productivityCompressedUnsynced = productivityCompressedUnsynced,
            emotionalCompressedUnsynced = emotionalCompressedUnsynced,
            cardioCompressedUnsynced = cardioCompressedUnsynced,
            EEGRAWCompressedUnsynced = EEGRAWCompressedUnsynced,
            EEGPROCEEDCompressedUnsynced = EEGPROCEEDCompressedUnsynced,
            EEGArtifactsCompressedUnsynced = EEGArtifactsCompressedUnsynced
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
        val EEGArtifactsCount: Int,
        val nfbCompressedCount: Int,
        val physiologicalCompressedCount: Int,
        val memsCompressedCount: Int,
        val productivityCompressedCount: Int,
        val emotionalCompressedCount: Int,
        val cardioCompressedCount: Int,
        val EEGRAWCompressedCount: Int,
        val EEGPROCEEDCompressedCount: Int,
        val EEGArtifactsCompressedCount: Int
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
    val nfbCompressedCount: Int,
    val physiologicalCompressedCount: Int,
    val memsCompressedCount: Int,
    val productivityCompressedCount: Int,
    val emotionalCompressedCount: Int,
    val cardioCompressedCount: Int,
    val EEGRAWCompressedCount: Int,
    val EEGPROCEEDCompressedCount: Int,
    val EEGArtifactsCompressedCount: Int,
    val nfbUnsynced: Int,
    val physiologicalUnsynced: Int,
    val memsUnsynced: Int,
    val productivityUnsynced: Int,
    val emotionalUnsynced: Int,
    val cardioUnsynced: Int,
    val EEGRAWUnsynced: Int,
    val EEGPROCEEDUnsynced: Int,
    val EEGArtifactsUnsynced: Int,
    val nfbCompressedUnsynced: Int,
    val physiologicalCompressedUnsynced: Int,
    val memsCompressedUnsynced: Int,
    val productivityCompressedUnsynced: Int,
    val emotionalCompressedUnsynced: Int,
    val cardioCompressedUnsynced: Int,
    val EEGRAWCompressedUnsynced: Int,
    val EEGPROCEEDCompressedUnsynced: Int,
    val EEGArtifactsCompressedUnsynced: Int
)