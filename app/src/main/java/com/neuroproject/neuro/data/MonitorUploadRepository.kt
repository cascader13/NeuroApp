package com.neuroproject.neuro.data

import android.util.Log
import com.neuroproject.neuro.data.remote.MonitorApiService
import com.neuroproject.neuro.data.remote.StringRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitorUploadRepository @Inject constructor(
    private val apiService: MonitorApiService
) {

    /**
     * Отправить строку на второй сервер.
     * Метод доступен для инъекции в любой компонент (ViewModel, Activity и т.д.).
     * ID генерируется автоматически.
     * Возвращает Flow для наблюдения за прогрессом/результатом (аналогично вашему uploadWithProgress).
     */
    // В MonitorUploadRepository.kt добавьте:
    suspend fun sendStringSimple(id: String?, text: String): Boolean {
        return try {
            val request = StringRequest(id = id, text = text)
            val response = apiService.sendString(request)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("MonitorUploadRepo", "Error sending string", e)
            false
        }
    }
}

// Классы для прогресса (аналогично вашему UploadProgress)
sealed class SendStringProgress {
    data class Preparing(val message: String) : SendStringProgress()
    data class Success(val message: String, val id: String?) : SendStringProgress()
    data class Error(val message: String) : SendStringProgress()
}