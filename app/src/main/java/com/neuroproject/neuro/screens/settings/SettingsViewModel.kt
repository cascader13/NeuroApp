// [file name]: SettingsViewModel.kt (для отправки)
package com.neuroproject.neuro.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
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
    private val uploadRepository: MetricsUploadRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    private val _effect = Channel<SettingsEffect>()

    private var uploadJob: kotlinx.coroutines.Job? = null

    init {
        loadStats()
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

    fun onKeyChanged(newKey: String) {
        val isValid = newKey.matches(Regex("^[a-zA-Z0-9]*\$"))

        _state.update {
            it.copy(
                key = newKey,
                isKeyValid = isValid,
                errorMessage = null,
                successMessage = null
            )
        }

        saveKey(newKey)
    }

    /**
     * Основной метод для отправки данных на сервер
     */
    fun onUploadClicked() {
        if (_state.value.isUploading) return

        uploadJob?.cancel()

        uploadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isUploading = true,
                    uploadProgress = 0f,
                    errorMessage = null,
                    successMessage = null,
                    currentStep = "Начинаем отправку данных..."
                )
            }

            try {
                uploadRepository.uploadWithProgress()
                    .onEach { progress ->
                        handleUploadProgress(progress)
                    }
                    .catch { e ->
                        _state.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = "Ошибка: ${e.message}",
                                currentStep = null
                            )
                        }
                    }
                    .collect()

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = "Неизвестная ошибка: ${e.message ?: "Неизвестная ошибка"}",
                        currentStep = null
                    )
                }

                delay(5000)
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    /**
     * Сохранить данные в JSON файл (для отладки)
     */
    fun saveToFile() {
        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        isUploading = true,
                        currentStep = "Сохранение в файл..."
                    )
                }

                val result = uploadRepository.saveToJsonFile()

                when (result) {
                    is FileSaveResult.NoData -> {
                        _state.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = "Нет данных для сохранения",
                                currentStep = null
                            )
                        }
                    }
                    is FileSaveResult.Success -> {
                        _state.update {
                            it.copy(
                                isUploading = false,
                                successMessage = "Файл сохранен: ${result.fileName}\nЗаписей: ${result.recordsCount}",
                                savedFilePath = result.filePath,
                                currentStep = null
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
                                currentStep = null
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = "Ошибка сохранения: ${e.message}",
                        currentStep = null
                    )
                }
            }
        }
    }

    private suspend fun handleUploadProgress(progress: UploadProgress) {
        when (progress) {
            is UploadProgress.Preparing -> {
                _state.update {
                    it.copy(
                        uploadProgress = progress.progress,
                        currentStep = progress.step
                    )
                }
            }

            is UploadProgress.Completed -> {
                // Обновляем статистику после успешной отправки
                loadStats()

                _state.update {
                    it.copy(
                        isUploading = false,
                        uploadProgress = 1f,
                        successMessage = createSuccessMessage(progress),
                        currentStep = null
                    )
                }

                delay(5000)
                _state.update { it.copy(successMessage = null) }
            }

            UploadProgress.NoData -> {
                _state.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = "Нет данных для отправки",
                        currentStep = null
                    )
                }
            }

            is UploadProgress.Error -> {
                _state.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = progress.message,
                        currentStep = null
                    )
                }
            }
        }
    }

    private fun createSuccessMessage(progress: UploadProgress.Completed): String {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        return """
            ✅ Отправка завершена в $time
            
            📤 Отправлено записей: ${progress.sentCount}
            
            💬 Ответ сервера: ${progress.message}
            
            📊 Данные помечены как отправленные
        """.trimIndent()
    }

    fun shareSavedFile() {
        _state.value.savedFilePath?.let { filePath ->
            viewModelScope.launch {
                _effect.send(SettingsEffect.ShareFile(filePath))
            }
        }
    }

    private fun saveKey(key: String) {
        viewModelScope.launch {
            // Сохраняем ключ для будущего использования
            // preferences.edit().putString("user_key", key).apply()
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

// Обновленный State
data class SettingsState(
    val key: String = "",
    val isKeyValid: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentStep: String? = null,

    // Статистика
    val totalRecords: Int = 0,
    val unsyncedRecords: Int = 0,
    val uploadStats: UploadStats? = null,

    // Сохраненные файлы
    val savedFilePath: String? = null,

    val appInfo: String = "NeuroAssessment v0.6.3"
)

sealed class SettingsEffect {
    object NavigateBack : SettingsEffect()
    data class ShareFile(val filePath: String) : SettingsEffect()
}