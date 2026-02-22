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
    private val uploadRepository: MetricsUploadRepository,
    @ApplicationContext private val context: Context
) : ViewModel()
{
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    private val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    private val _effect = Channel<SettingsEffect>()
    private var uploadJob: kotlinx.coroutines.Job? = null

    init {
        loadSavedMobileId()
        loadSavedExpeditionId()
        loadStats()
    }


    private fun loadSavedMobileId() {
        // Сначала пробуем загрузить mobile_id
        var mobileId = sharedPreferences.getString("saved_mobile_id", "")

        // Если mobile_id пуст, пробуем загрузить user_id
        if (mobileId.isNullOrEmpty()) {
            mobileId = sharedPreferences.getString("saved_user_id", "")
        }

        if (!mobileId.isNullOrEmpty()) {
            _state.update {
                it.copy(mobileId = mobileId)
            }
        }
    }

    private fun loadSavedExpeditionId() {
        val expeditionId = sharedPreferences.getString("saved_expedition_id", "")
        if (!expeditionId.isNullOrEmpty()) {
            _state.update {
                it.copy(expeditionId = expeditionId)
            }
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


    private fun saveMobileIdToPreferences(id: String) {
        sharedPreferences.edit()
            .putString("saved_mobile_id", id)
            .apply()
    }

    private fun saveExpeditionIdToPreferences(id: String) {
        sharedPreferences.edit()
            .putString("saved_expedition_id", id)
            .apply()
    }

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
            Отправка завершена в $time
            
            Отправлено записей: ${progress.sentCount}
            
            Ответ сервера: ${progress.message}
            
            Данные помечены как отправленные
        """.trimIndent()
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

data class SettingsState(
    val mobileId: String = "",
    val expeditionId: String = "",
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentStep: String? = null,
    val totalRecords: Int = 0,
    val unsyncedRecords: Int = 0,
    val uploadStats: UploadStats? = null,
    val savedFilePath: String? = null,
    val appInfo: String = "NeuroAssessment v0.6.3"
)

sealed class SettingsEffect {
    object NavigateBack : SettingsEffect()
    data class ShareFile(val filePath: String) : SettingsEffect()
}