package com.neuroproject.neuro.data

import android.util.Log
import com.neuroproject.neuro.data.remote.MonitorApiService
import com.neuroproject.neuro.data.remote.StringRequest
import com.neuroproject.neuro.services.DynamicServerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для отправки строковых сообщений на сервер мониторинга.
 *
 * Используется для отправки отдельных показателей в реальном времени.
 * Данный функционал не будет присутствовать в релизной версии.
 *
 * @see DynamicServerManager
 * @see MonitorApiService
 */
@Singleton
class MonitorUploadRepository @Inject constructor(
    private val dynamicServerManager: DynamicServerManager
) {

    /**
     * Отправить строку на второй сервер.
     * ID генерируется автоматически.
     */
    suspend fun sendStringSimple(id: String?, text: String): Boolean {
        return try {
            val apiService = dynamicServerManager.getApiService()
            val request = StringRequest(id = id, text = text)
            val response = apiService.sendString(request)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("MonitorUploadRepo", "Error sending string", e)
            false
        }
    }

    /**
     * Отправить строку с возможностью обновления адреса сервера
     */
    suspend fun sendStringWithCustomServer(serverAddress: String, id: String?, text: String): Boolean {
        return try {
            // Обновляем адрес сервера и получаем новый API service
            val apiService = dynamicServerManager.updateServerAddress(serverAddress)
            val request = StringRequest(id = id, text = text)
            val response = apiService.sendString(request)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("MonitorUploadRepo", "Error sending string to custom server", e)
            false
        }
    }
}

// Классы для прогресса
sealed class SendStringProgress {
    data class Preparing(val message: String) : SendStringProgress()
    data class Success(val message: String, val id: String?) : SendStringProgress()
    data class Error(val message: String) : SendStringProgress()
}