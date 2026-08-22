package com.neuroproject.neuro.data.remote

import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * Запрос на отправку строкового сообщения на сервер мониторинга(будет исключено из релизной версии)
 *
 * Используется для передачи отдельных значений метрик в реальном времени.
 * Позволяет серверу идентифицировать пользователя и получить значение метрики
 * в текстовом формате.
 *
 * ## Формат сообщения:
 * ```
 * {
 *   "id": "user_12345",
 *   "text": "concentration 0.85"
 * }
 * ```
 *
 * @property id Идентификатор пользователя (из SharedPreferences "saved_user_id")
 * @property text Текст сообщения в формате "метрика значение"
 *
 * @see MonitorApiService
 */
data class StringRequest(
    /**
     * Уникальный идентификатор пользователя
     *
     * Извлекается из SharedPreferences по ключу "saved_user_id".
     * Используется сервером для привязки данных к конкретному пользователю.
     */
    @SerializedName("id")
    val id: String?,

    /**
     * Текстовое представление метрики
     *
     * Формат: "название_метрики значение"
     *
     * Примеры:
     * - "concentration 0.85" - уровень концентрации 85%
     * - "relax 0.72" - уровень расслабления 72%
     * - "stress 0.3" - уровень стресса 30%
     */
    @SerializedName("text")
    val text: String
)

/**
 * Ответ сервера на строковый запрос
 *
 * @property success Флаг успешной обработки сообщения сервером
 */
data class StringResponse(
    @SerializedName("success")
    val success: Boolean
)