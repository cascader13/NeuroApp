package com.neuroproject.neuro.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API сервис для отправки метрик нейро-гарнитуры на сервер
 *
 * Предоставляет методы для загрузки собранных данных (метрик) с устройства на удаленный сервер.
 * Используется для синхронизации данных после завершения сессии записи.
 *
 * ## Формат данных:
 * - Все метрики передаются в одном запросе через [UploadRequest]
 * - Сервер ожидает массив данных различных типов (NFB, ЭЭГ, физиологические и т.д.)
 * - Поддерживаются как сжатые, так и несжатые форматы данных
 *
 * ## Пример использования:
 * ```kotlin
 * class MetricsUploader @Inject constructor(
 *     private val api: MetricsApiService
 * ) {
 *     suspend fun uploadMetrics(metrics: UploadRequest): Boolean {
 *         val response = api.uploadMetrics(metrics)
 *         return response.isSuccessful && response.body()?.result == true
 *     }
 * }
 * ```
 *
 * @see UploadRequest
 * @see UploadResponse
 */
interface MetricsApiService {
    /**
     * Отправка метрик на сервер
     *
     * Загружает все накопленные метрики (NFB, ЭЭГ, физиологические, кардио и др.)
     * единым запросом(это в скором времени переделается)
     *
     * @param request Объект с данными метрик для отправки
     * @return Ответ сервера с результатом операции
     *
     * @throws IOException При ошибках сети
     * @throws HttpException При HTTP ошибках (4xx, 5xx)
     */
    @POST("/api/metrics/upload")
    suspend fun uploadMetrics(
        @Body request: UploadRequest
    ): Response<UploadResponse>
}

/**
 * Ответ сервера на запрос загрузки метрик
 *
 * @property result Флаг успешности операции (true - данные приняты, false - ошибка)
 */
data class UploadResponse(
    val result: Boolean,
    val message: String? = null,
    val acceptedCount: Int? = null,
    val batchId: String? = null
)