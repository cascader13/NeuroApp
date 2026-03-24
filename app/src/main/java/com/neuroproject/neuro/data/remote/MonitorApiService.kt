package com.neuroproject.neuro.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API сервис для отправки простых строковых сообщений на сервер мониторинга(данный функционал не будет присутствовать в релизе)
 *
 * Используется для отправки отдельных показателей в реальном времени,
 * например, для отладки или оперативного мониторинга состояния пользователя.
 *
 *
 * ## Пример использования:
 * ```kotlin
 * class MonitorUploadRepository @Inject constructor(
 *     private val api: MonitorApiService
 * ) {
 *     suspend fun sendString(userId: String, text: String): Boolean {
 *         val response = api.sendString(StringRequest(userId, text))
 *         return response.isSuccessful && response.body()?.success == true
 *     }
 * }
 * ```
 *
 * @see StringRequest
 * @see StringResponse
 */
interface MonitorApiService {
    /**
     * Отправка строкового сообщения на сервер
     *
     * Позволяет отправлять одиночные значения метрик в реальном времени.
     *
     * @param request Строковый запрос с ID пользователя и текстом сообщения
     * @return Ответ сервера с флагом успешности
     *
     * @throws IOException При ошибках сети
     * @throws HttpException При HTTP ошибках (4xx, 5xx)
     */
    @POST("/api/sendString")
    suspend fun sendString(
        @Body request: StringRequest
    ): Response<StringResponse>
}