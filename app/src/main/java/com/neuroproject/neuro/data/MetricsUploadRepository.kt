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
     * Подготовить данные для отправки
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
                physiologicalBaselines, productivityBaselines, productivityIndexes
            )

            if (totalRecords == 0) {
                return UploadPreparationResult.NoData
            }

            // Создаем DTO для отправки
            val uploadRequest = UploadRequest(
                // Uncompressed данные
                nfbMetrics = nfbMetrics.map { it.toServerDto() },
                physiologicalMetrics = physiologicalMetrics.map { it.toServerDto() },
                eegRawMetrics = eegRawMetrics.map { it.toServerDto() },
                eegProceedMetrics = eegProceedMetrics.map { it.toServerDto() },
                eegArtifactsMetrics = eegArtifactsMetrics.map { it.toServerDto() },
                memsMetrics = memsMetrics.map { it.toServerDto() },
                productivityMetrics = productivityMetrics.map { it.toServerDto() },
                emotionalMetrics = emotionalMetrics.map { it.toServerDto() },
                cardioMetrics = cardioMetrics.map { it.toServerDto() },

                // Compressed данные
                nfbMetricsCompressed = nfbMetricsCompressed.map { it.toServerDto() },
                physiologicalMetricsCompressed = physiologicalMetricsCompressed.map { it.toServerDto() },
                eegRawMetricsCompressed = eegRawMetricsCompressed.map { it.toServerDto() },
                eegProceedMetricsCompressed = eegProceedMetricsCompressed.map { it.toServerDto() },
                eegArtifactsMetricsCompressed = eegArtifactsMetricsCompressed.map { it.toServerDto() },
                memsMetricsCompressed = memsMetricsCompressed.map { it.toServerDto() },
                productivityMetricsCompressed = productivityMetricsCompressed.map { it.toServerDto() },
                emotionalMetricsCompressed = emotionalMetricsCompressed.map { it.toServerDto() },
                cardioMetricsCompressed = cardioMetricsCompressed.map { it.toServerDto() },

                // Baseline и Indexes данные
                physiologicalBaselines = physiologicalBaselines.map { it.toServerDto() },
                productivityBaselines = productivityBaselines.map { it.toServerDto() },
                productivityIndexes = productivityIndexes.map { it.toServerDto() }
            )

            UploadPreparationResult.Ready(
                request = uploadRequest,
                totalRecords = totalRecords,
                // Uncompressed counts
                nfbCount = nfbMetrics.size,
                physiologicalCount = physiologicalMetrics.size,
                eegRawCount = eegRawMetrics.size,
                eegProceedCount = eegProceedMetrics.size,
                eegArtifactsCount = eegArtifactsMetrics.size,
                memsCount = memsMetrics.size,
                productivityCount = productivityMetrics.size,
                emotionalCount = emotionalMetrics.size,
                cardioCount = cardioMetrics.size,
                // Compressed counts
                nfbCompressedCount = nfbMetricsCompressed.size,
                physiologicalCompressedCount = physiologicalMetricsCompressed.size,
                eegRawCompressedCount = eegRawMetricsCompressed.size,
                eegProceedCompressedCount = eegProceedMetricsCompressed.size,
                eegArtifactsCompressedCount = eegArtifactsMetricsCompressed.size,
                memsCompressedCount = memsMetricsCompressed.size,
                productivityCompressedCount = productivityMetricsCompressed.size,
                emotionalCompressedCount = emotionalMetricsCompressed.size,
                cardioCompressedCount = cardioMetricsCompressed.size,
                // Baseline counts
                physiologicalBaselinesCount = physiologicalBaselines.size,
                productivityBaselinesCount = productivityBaselines.size,
                productivityIndexesCount = productivityIndexes.size
            )

        } catch (e: Exception) {
            UploadPreparationResult.Error("Ошибка подготовки данных: ${e.message}")
        }
    }

    private fun calculateTotalRecords(vararg lists: List<*>): Int {
        return lists.sumOf { it.size }
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
                        markDataAsUploaded(preparationResult)
                        UploadResult.Success(
                            sentCount = preparationResult.totalRecords
                        )
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
     * Пометить данные как отправленные
     */
    private suspend fun markDataAsUploaded(preparationResult: UploadPreparationResult.Ready) {
        // Uncompressed timestamps
        if (preparationResult.request.nfbMetrics?.isNotEmpty() == true) {
            metricsDao.safeMarkNFBMetricsAsSynced(preparationResult.request.nfbMetrics.map { it.timestamp })
        }
        if (preparationResult.request.physiologicalMetrics?.isNotEmpty() == true) {
            metricsDao.safeMarkPhysiologicalMetricsAsSynced(preparationResult.request.physiologicalMetrics.map { it.timestamp })
        }
        if (preparationResult.request.eegRawMetrics?.isNotEmpty() == true) {
            metricsDao.safeMarkEEGRAWMetricsAsSynced(preparationResult.request.eegRawMetrics.map { it.timestamp })
        }
        if (preparationResult.request.eegProceedMetrics?.isNotEmpty() == true) {
            metricsDao.safeMarkEEGProceedMetricsAsSynced(preparationResult.request.eegProceedMetrics.map { it.timestamp })
        }
        if (preparationResult.request.eegArtifactsMetrics?.isNotEmpty() == true) {
            metricsDao.safeMarkEEGArtifactsMetricsAsSynced(preparationResult.request.eegArtifactsMetrics.map { it.timestamp })
        }
        if (preparationResult.request.memsMetrics?.isNotEmpty() == true) {
            metricsDao.safeMarkMEMSMetricsAsSynced(preparationResult.request.memsMetrics.map { it.timestamp })
        }
        if (preparationResult.request.productivityMetrics?.isNotEmpty() == true) {
            metricsDao.safeMarkProductivityMetricsAsSynced(preparationResult.request.productivityMetrics.map { it.timestamp })
        }
        if (preparationResult.request.emotionalMetrics?.isNotEmpty() == true) {
            metricsDao.safeMarkEmotionalMetricsAsSynced(preparationResult.request.emotionalMetrics.map { it.timestamp })
        }
        if (preparationResult.request.cardioMetrics?.isNotEmpty() == true) {
            metricsDao.safeMarkCardioMetricsAsSynced(preparationResult.request.cardioMetrics.map { it.timestamp })
        }

        // Compressed timestamps
        if (preparationResult.request.nfbMetricsCompressed?.isNotEmpty() == true) {
            metricsDao.safeMarkNFBMetricsCompressedAsSynced(preparationResult.request.nfbMetricsCompressed.map { it.timestamp })
        }
        if (preparationResult.request.physiologicalMetricsCompressed?.isNotEmpty() == true) {
            metricsDao.safeMarkPhysiologicalMetricsCompressedAsSynced(preparationResult.request.physiologicalMetricsCompressed.map { it.timestamp })
        }
        if (preparationResult.request.eegRawMetricsCompressed?.isNotEmpty() == true) {
            metricsDao.safeMarkEEGRAWMetricsCompressedAsSynced(preparationResult.request.eegRawMetricsCompressed.map { it.timestamp })
        }
        if (preparationResult.request.eegProceedMetricsCompressed?.isNotEmpty() == true) {
            metricsDao.safeMarkEEGProceedMetricsCompressedAsSynced(preparationResult.request.eegProceedMetricsCompressed.map { it.timestamp })
        }
        if (preparationResult.request.eegArtifactsMetricsCompressed?.isNotEmpty() == true) {
            metricsDao.safeMarkEEGArtifactsMetricsCompressedAsSynced(preparationResult.request.eegArtifactsMetricsCompressed.map { it.timestamp })
        }
        if (preparationResult.request.memsMetricsCompressed?.isNotEmpty() == true) {
            metricsDao.safeMarkMEMSMetricsCompressedAsSynced(preparationResult.request.memsMetricsCompressed.map { it.timestamp })
        }
        if (preparationResult.request.productivityMetricsCompressed?.isNotEmpty() == true) {
            metricsDao.safeMarkProductivityMetricsCompressedAsSynced(preparationResult.request.productivityMetricsCompressed.map { it.timestamp })
        }
        if (preparationResult.request.emotionalMetricsCompressed?.isNotEmpty() == true) {
            metricsDao.safeMarkEmotionalMetricsCompressedAsSynced(preparationResult.request.emotionalMetricsCompressed.map { it.timestamp })
        }
        if (preparationResult.request.cardioMetricsCompressed?.isNotEmpty() == true) {
            metricsDao.safeMarkCardioMetricsCompressedAsSynced(preparationResult.request.cardioMetricsCompressed.map { it.timestamp })
        }

        // Baseline timestamps
        if (preparationResult.request.physiologicalBaselines?.isNotEmpty() == true) {
            metricsDao.safeMarkPhysiologicalBaselineAsSynced(preparationResult.request.physiologicalBaselines.map { it.timestamp })
        }
        if (preparationResult.request.productivityBaselines?.isNotEmpty() == true) {
            metricsDao.safeMarkProductivityBaselineAsSynced(preparationResult.request.productivityBaselines.map { it.timestamp })
        }
        if (preparationResult.request.productivityIndexes?.isNotEmpty() == true) {
            metricsDao.safeMarkProductivityIndexesAsSynced(preparationResult.request.productivityIndexes.map { it.timestamp })
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

    /**
     * Получить статистику
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

        // Baseline counts
        val physiologicalBaselinesCount = metricsDao.getAllPhysiologicalBaselineCount()
        val productivityBaselinesCount = metricsDao.getAllProductivityBaselineCount()
        val productivityIndexesCount = metricsDao.getAllProductivityIndexesCount()

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

        // Baseline unsynced counts
        val physiologicalBaselinesUnsynced = metricsDao.getUnmarkedPhysiologicalBaseline().size
        val productivityBaselinesUnsynced = metricsDao.getUnmarkedProductivityBaseline().size
        val productivityIndexesUnsynced = metricsDao.getUnmarkedProductivityIndexes().size

        val totalRecords = nfbCount + physiologicalCount + eegRawCount + eegProceedCount +
                eegArtifactsCount + memsCount + productivityCount + emotionalCount + cardioCount +
                nfbCompressedCount + physiologicalCompressedCount + eegRawCompressedCount +
                eegProceedCompressedCount + eegArtifactsCompressedCount + memsCompressedCount +
                productivityCompressedCount + emotionalCompressedCount + cardioCompressedCount +
                physiologicalBaselinesCount + productivityBaselinesCount + productivityIndexesCount

        val unsyncedRecords = nfbUnsynced + physiologicalUnsynced + eegRawUnsynced + eegProceedUnsynced +
                eegArtifactsUnsynced + memsUnsynced + productivityUnsynced + emotionalUnsynced + cardioUnsynced +
                nfbCompressedUnsynced + physiologicalCompressedUnsynced + eegRawCompressedUnsynced +
                eegProceedCompressedUnsynced + eegArtifactsCompressedUnsynced + memsCompressedUnsynced +
                productivityCompressedUnsynced + emotionalCompressedUnsynced + cardioCompressedUnsynced +
                physiologicalBaselinesUnsynced + productivityBaselinesUnsynced + productivityIndexesUnsynced

        return UploadStats(
            totalRecords = totalRecords,
            unsyncedRecords = unsyncedRecords,
            // Uncompressed counts
            nfbCount = nfbCount,
            physiologicalCount = physiologicalCount,
            eegRawCount = eegRawCount,
            eegProceedCount = eegProceedCount,
            eegArtifactsCount = eegArtifactsCount,
            memsCount = memsCount,
            productivityCount = productivityCount,
            emotionalCount = emotionalCount,
            cardioCount = cardioCount,
            // Compressed counts
            nfbCompressedCount = nfbCompressedCount,
            physiologicalCompressedCount = physiologicalCompressedCount,
            eegRawCompressedCount = eegRawCompressedCount,
            eegProceedCompressedCount = eegProceedCompressedCount,
            eegArtifactsCompressedCount = eegArtifactsCompressedCount,
            memsCompressedCount = memsCompressedCount,
            productivityCompressedCount = productivityCompressedCount,
            emotionalCompressedCount = emotionalCompressedCount,
            cardioCompressedCount = cardioCompressedCount,
            // Baseline counts
            physiologicalBaselinesCount = physiologicalBaselinesCount,
            productivityBaselinesCount = productivityBaselinesCount,
            productivityIndexesCount = productivityIndexesCount,
            // Unsynced counts
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
            productivityIndexesUnsynced = productivityIndexesUnsynced
        )
    }
}

// Классы данных для подготовки
sealed class UploadPreparationResult {
    object NoData : UploadPreparationResult()
    data class Ready(
        val request: UploadRequest,
        val totalRecords: Int,
        // Uncompressed counts
        val nfbCount: Int,
        val physiologicalCount: Int,
        val eegRawCount: Int,
        val eegProceedCount: Int,
        val eegArtifactsCount: Int,
        val memsCount: Int,
        val productivityCount: Int,
        val emotionalCount: Int,
        val cardioCount: Int,
        // Compressed counts
        val nfbCompressedCount: Int,
        val physiologicalCompressedCount: Int,
        val eegRawCompressedCount: Int,
        val eegProceedCompressedCount: Int,
        val eegArtifactsCompressedCount: Int,
        val memsCompressedCount: Int,
        val productivityCompressedCount: Int,
        val emotionalCompressedCount: Int,
        val cardioCompressedCount: Int,
        // Baseline counts
        val physiologicalBaselinesCount: Int,
        val productivityBaselinesCount: Int,
        val productivityIndexesCount: Int
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
    // Uncompressed counts
    val nfbCount: Int,
    val physiologicalCount: Int,
    val eegRawCount: Int,
    val eegProceedCount: Int,
    val eegArtifactsCount: Int,
    val memsCount: Int,
    val productivityCount: Int,
    val emotionalCount: Int,
    val cardioCount: Int,
    // Compressed counts
    val nfbCompressedCount: Int,
    val physiologicalCompressedCount: Int,
    val eegRawCompressedCount: Int,
    val eegProceedCompressedCount: Int,
    val eegArtifactsCompressedCount: Int,
    val memsCompressedCount: Int,
    val productivityCompressedCount: Int,
    val emotionalCompressedCount: Int,
    val cardioCompressedCount: Int,
    // Baseline counts
    val physiologicalBaselinesCount: Int,
    val productivityBaselinesCount: Int,
    val productivityIndexesCount: Int,
    // Unsynced counts
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
    val productivityIndexesUnsynced: Int
)