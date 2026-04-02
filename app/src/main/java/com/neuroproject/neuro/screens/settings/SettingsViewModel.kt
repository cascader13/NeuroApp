package com.neuroproject.neuro.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val uploadRepository: MetricsUploadRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    private val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    private val _effect = Channel<SettingsEffect>()
    private var uploadJob: kotlinx.coroutines.Job? = null

    init {
        loadSavedMobileId()
        loadSavedExpeditionId()
        loadServerAddress()
        loadStats()
    }

    private fun loadSavedMobileId() {
        var mobileId = sharedPreferences.getString("saved_mobile_id", "")
        if (mobileId.isNullOrEmpty()) {
            mobileId = sharedPreferences.getString("saved_user_id", "")
        }
        if (!mobileId.isNullOrEmpty()) {
            _state.update { it.copy(mobileId = mobileId) }
        }
    }

    private fun loadSavedExpeditionId() {
        val expeditionId = sharedPreferences.getString("saved_expedition_id", "")
        if (!expeditionId.isNullOrEmpty()) {
            _state.update { it.copy(expeditionId = expeditionId) }
        }
    }

    private fun loadServerAddress() {
        val serverAddress = sharedPreferences.getString("server_address", "http://10.240.68.80:5000")
        if (!serverAddress.isNullOrEmpty()) {
            _state.update { it.copy(serverAddress = serverAddress) }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val stats = uploadRepository.getStats()
                _state.update {
                    it.copy(
                        totalRecords = stats.totalRecords,
                        unsyncedRecords = stats.unsyncedRecords,
                        uploadStats = stats
                    )
                }
            } catch (e: Exception) {
                // Логируем ошибку
            }
        }
    }

    fun onMobileIdChanged(newId: String) {
        saveMobileIdToPreferences(newId)
        _state.update {
            it.copy(
                mobileId = newId,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onExpeditionIdChanged(newId: String) {
        saveExpeditionIdToPreferences(newId)
        _state.update {
            it.copy(
                expeditionId = newId,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onServerAddressChanged(newAddress: String) {
        saveServerAddressToPreferences(newAddress)
        _state.update {
            it.copy(
                serverAddress = newAddress,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun saveMobileIdToPreferences(id: String) {
        sharedPreferences.edit().putString("saved_mobile_id", id).apply()
    }

    private fun saveExpeditionIdToPreferences(id: String) {
        sharedPreferences.edit().putString("saved_expedition_id", id).apply()
    }

    private fun saveServerAddressToPreferences(address: String) {
        sharedPreferences.edit().putString("server_address", address).apply()
    }

    // ==================== ОБНОВЛЕННЫЙ МЕТОД ОТПРАВКИ ====================

    fun onUploadClicked() {
        if (_state.value.isUploading) return

        uploadJob?.cancel()

        uploadJob = viewModelScope.launch {
            // Сбрасываем состояние
            _state.update {
                it.copy(
                    isUploading = true,
                    uploadProgress = 0f,
                    uploadProgressText = "Начинаем отправку...",
                    errorMessage = null,
                    successMessage = null,
                    currentStep = "Подготовка данных...",
                    // Сбрасываем детальную информацию о пакетах
                    totalBatches = 0,
                    currentBatch = 0,
                    currentBatchRecords = 0,
                    totalSentRecords = 0,
                    failedBatches = 0,
                    uploadStatus = UploadStatus.Preparing
                )
            }

            try {
                // Используем новую пакетную отправку
                uploadRepository.uploadInBatches(
                    batchSize = 100,      // 100 записей на пакет
                    enableRetry = true,   // Включить повторные попытки
                    stopOnError = false,  // Не останавливаться при ошибке
                    batchDelayMs = 500    // 500 мс между пакетами
                )
                    .onEach { progress ->
                        handleBatchUploadProgress(progress)
                    }
                    .catch { e ->
                        _state.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = "Ошибка: ${e.message}",
                                currentStep = null,
                                uploadStatus = UploadStatus.Error
                            )
                        }
                    }
                    .collect { /* Прогресс уже обработан в onEach */ }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = "Неизвестная ошибка: ${e.message ?: "Неизвестная ошибка"}",
                        currentStep = null,
                        uploadStatus = UploadStatus.Error
                    )
                }
                delay(5000)
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    /**
     * Обработка прогресса пакетной отправки
     */
    private suspend fun handleBatchUploadProgress(progress: BatchUploadProgress) {
        when (progress) {
            is BatchUploadProgress.Preparing -> {
                _state.update {
                    it.copy(
                        uploadProgress = progress.progress,
                        currentStep = progress.step,
                        uploadProgressText = progress.step,
                        uploadStatus = UploadStatus.Preparing
                    )
                }
            }

            is BatchUploadProgress.BatchesCreated -> {
                _state.update {
                    it.copy(
                        totalBatches = progress.totalBatches,
                        totalRecords = progress.totalRecords,
                        uploadProgress = 0.1f,
                        currentStep = "Создано ${progress.totalBatches} пакетов",
                        uploadProgressText = "Создано ${progress.totalBatches} пакетов",
                        uploadStatus = UploadStatus.Preparing
                    )
                }
            }

            is BatchUploadProgress.SendingBatch -> {
                val overallProgress = 0.1f + (0.8f * (progress.current - 1) / progress.total)

                _state.update {
                    it.copy(
                        currentBatch = progress.current,
                        totalBatches = progress.total,
                        currentBatchRecords = progress.recordsInBatch,
                        uploadProgress = overallProgress,
                        currentStep = "Отправка пакета ${progress.current}/${progress.total} (${progress.recordsInBatch} записей)",
                        uploadProgressText = "Отправка пакета ${progress.current}/${progress.total}",
                        uploadStatus = UploadStatus.Sending
                    )
                }
            }

            is BatchUploadProgress.BatchCompleted -> {
                val overallProgress = 0.1f + (0.8f * progress.batchIndex / progress.totalBatches)

                _state.update {
                    it.copy(
                        totalSentRecords = progress.totalSentSoFar,
                        uploadProgress = overallProgress,
                        currentStep = "Пакет ${progress.batchIndex}/${progress.totalBatches} отправлен (${progress.totalSentSoFar} записей всего)",
                        uploadProgressText = "Отправлено ${progress.totalSentSoFar} записей",
                        uploadStatus = UploadStatus.Sending
                    )
                }
            }

            is BatchUploadProgress.BatchFailed -> {
                _state.update {
                    it.copy(
                        failedBatches = it.failedBatches + 1,
                        currentStep = "Ошибка пакета ${progress.batchIndex}: ${progress.error}",
                        uploadStatus = UploadStatus.Error
                    )
                }

                // Показываем ошибку, но не останавливаем отправку
                _state.update {
                    it.copy(
                        errorMessage = "Ошибка пакета ${progress.batchIndex}: ${progress.error}"
                    )
                }

                delay(3000)
                _state.update { it.copy(errorMessage = null) }
            }

            is BatchUploadProgress.PartialSuccess -> {
                loadStats() // Обновляем статистику

                _state.update {
                    it.copy(
                        isUploading = false,
                        uploadProgress = 0.9f,
                        totalSentRecords = progress.sentCount,
                        successMessage = createPartialSuccessMessage(progress),
                        currentStep = null,
                        uploadProgressText = "Отправка завершена частично",
                        uploadStatus = UploadStatus.PartialSuccess
                    )
                }

                delay(5000)
                _state.update { it.copy(successMessage = null) }
            }

            is BatchUploadProgress.Completed -> {
                loadStats() // Обновляем статистику

                _state.update {
                    it.copy(
                        isUploading = false,
                        uploadProgress = 1f,
                        totalSentRecords = progress.sentCount,
                        successMessage = createSuccessMessage(progress),
                        currentStep = null,
                        uploadProgressText = "Отправка завершена",
                        uploadStatus = UploadStatus.Success
                    )
                }

                delay(5000)
                _state.update { it.copy(successMessage = null) }
            }

            is BatchUploadProgress.Stopped -> {
                loadStats()

                _state.update {
                    it.copy(
                        isUploading = false,
                        uploadProgress = 0f,
                        totalSentRecords = progress.sentCount,
                        errorMessage = "Отправка остановлена: ${progress.reason}",
                        currentStep = null,
                        uploadStatus = UploadStatus.Error
                    )
                }

                delay(5000)
                _state.update { it.copy(errorMessage = null) }
            }

            BatchUploadProgress.NoData -> {
                _state.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = "Нет данных для отправки",
                        currentStep = null,
                        uploadStatus = UploadStatus.NoData
                    )
                }
            }

            is BatchUploadProgress.Error -> {
                _state.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = progress.message,
                        currentStep = null,
                        uploadStatus = UploadStatus.Error
                    )
                }
            }
        }
    }

    private fun createSuccessMessage(progress: BatchUploadProgress.Completed): String {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        return """
            Отправка успешно завершена!
            
            Отправлено: ${progress.sentCount} записей
            Пакетов: ${progress.totalBatches}
            Время: $time
            
            Все данные успешно переданы на сервер
        """.trimIndent()
    }

    private fun createPartialSuccessMessage(progress: BatchUploadProgress.PartialSuccess): String {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        return """
            Отправка завершена с ошибками
            
            Успешно: ${progress.sentCount} из ${progress.totalCount} записей
            Не отправлено: ${progress.failedRecords} записей (${progress.failedBatches} пакетов)
            Время: $time
            
            Неотправленные данные будут отправлены при следующей попытке
        """.trimIndent()
    }

    fun saveToFile() {
        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        isUploading = true,
                        currentStep = "Сохранение в файл...",
                        uploadStatus = UploadStatus.Saving
                    )
                }

                val result = uploadRepository.saveToJsonFile(saveAsBatches = true)

                when (result) {
                    is FileSaveResult.NoData -> {
                        _state.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = "Нет данных для сохранения",
                                currentStep = null,
                                uploadStatus = UploadStatus.Error
                            )
                        }
                    }
                    is FileSaveResult.Success -> {
                        _state.update {
                            it.copy(
                                isUploading = false,
                                successMessage = "Файлы сохранены: ${result.fileName}\nЗаписей: ${result.recordsCount}\nПапка: ${result.filePath}",
                                savedFilePath = result.filePath,
                                currentStep = null,
                                uploadStatus = UploadStatus.Success
                            )
                        }

                        delay(5000)
                        _state.update { it.copy(successMessage = null) }
                    }
                    is FileSaveResult.Error -> {
                        _state.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = result.message,
                                currentStep = null,
                                uploadStatus = UploadStatus.Error
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = "Ошибка сохранения: ${e.message}",
                        currentStep = null,
                        uploadStatus = UploadStatus.Error
                    )
                }
            }
        }
    }

    fun shareSavedFile() {
        _state.value.savedFilePath?.let { filePath ->
            viewModelScope.launch {
                _effect.send(SettingsEffect.ShareFile(filePath))
            }
        }
    }

    fun onBackClicked() {
        uploadJob?.cancel()
        viewModelScope.launch {
            _effect.send(SettingsEffect.NavigateBack)
        }
    }

    override fun onCleared() {
        super.onCleared()
        uploadJob?.cancel()
    }
}

// ==================== ОБНОВЛЕННЫЙ STATE ====================

data class SettingsState(
    val mobileId: String = "",
    val expeditionId: String = "",
    val serverAddress: String = "http://10.240.68.80:5000",
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val uploadProgressText: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentStep: String? = null,
    val totalRecords: Int = 0,
    val unsyncedRecords: Int = 0,
    val uploadStats: UploadStats? = null,
    val savedFilePath: String? = null,
    val appInfo: String = "NeuroAssessment v0.6.3",
    // Новые поля для пакетной отправки
    val totalBatches: Int = 0,
    val currentBatch: Int = 0,
    val currentBatchRecords: Int = 0,
    val totalSentRecords: Int = 0,
    val failedBatches: Int = 0,
    val uploadStatus: UploadStatus = UploadStatus.Idle
)

enum class UploadStatus {
    Idle,           // Ожидание
    Preparing,      // Подготовка
    Sending,        // Отправка
    Success,        // Успешно
    PartialSuccess, // Частичный успех
    Error,          // Ошибка
    NoData,         // Нет данных
    Saving          // Сохранение в файл
}

sealed class SettingsEffect {
    object NavigateBack : SettingsEffect()
    data class ShareFile(val filePath: String) : SettingsEffect()
}