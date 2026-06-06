package com.neuroproject.neuro.services

import android.content.Context
import com.neuroproject.neuro.SecondServer
import com.neuroproject.neuro.data.remote.MonitorApiService
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Менеджер динамической конфигурации сервера мониторинга(в релиз не попадёт)
 *
 * Этот класс обеспечивает гибкое управление адресами сервера для API мониторинга.
 * Позволяет динамически изменять адрес сервера без перекомпиляции приложения,
 * сохраняя настройки в SharedPreferences.
 *
 * ## Основные возможности:
 * - Хранение адреса сервера в SharedPreferences
 * - Динамическое создание Retrofit клиента для нового адреса
 * - Ленивая инициализация API сервиса
 * - Поддержка нескольких серверов через квалификаторы Dagger Hilt
 *
 * ## Пример использования:
 * ```kotlin
 * class MyViewModel @Inject constructor(
 *     private val serverManager: DynamicServerManager
 * ) {
 *     fun sendData() {
 *         val api = serverManager.getApiService()
 *         // Использование api для отправки данных
 *     }
 *
 *     fun switchServer(newAddress: String) {
 *         val newApi = serverManager.updateServerAddress(newAddress)
 *         // Продолжение работы с новым сервером
 *     }
 * }
 * ```
 *
 * @property context Контекст приложения (инжектируется через Dagger Hilt)
 * @property gson Экземпляр Gson с аннотацией @SecondServer для парсинга JSON
 * @property okHttpClient HTTP клиент с аннотацией @SecondServer для сетевых запросов
 * @see MonitorApiService
 * @see SecondServer
 */
@Singleton
class DynamicServerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @SecondServer private val gson: Gson,
    @SecondServer private val okHttpClient: OkHttpClient
) {
    /** SharedPreferences для хранения адреса сервера */
    private val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    /** Кэшированный экземпляр API сервиса для текущего сервера */
    private var currentApiService: MonitorApiService? = null

    /**
     * Получение текущего API сервиса
     *
     * Возвращает экземпляр [MonitorApiService], настроенный на текущий адрес сервера.
     * При первом вызове создает новый экземпляр, при последующих возвращает кэшированный.
     *
     * @return Экземпляр API сервиса для текущего сервера
     *
     * @throws IllegalArgumentException если базовый URL некорректен
     */
    fun getApiService(): MonitorApiService {
        val serverAddress = getCurrentServerAddress()

        if (currentApiService == null) {
            currentApiService = createApiService(serverAddress)
        }

        return currentApiService!!
    }

    /**
     * Обновление адреса сервера
     *
     * Сохраняет новый адрес сервера в SharedPreferences и создает новый экземпляр API сервиса.
     * Используется при смене сервера во время работы приложения.
     *
     * @param newAddress Новый адрес сервера (например, "https://api.example.com/")
     * @return Новый экземпляр API сервиса для обновленного адреса
     *
     * @throws IllegalArgumentException если базовый URL некорректен
     */
    fun updateServerAddress(newAddress: String): MonitorApiService {
        saveServerAddress(newAddress)
        currentApiService = createApiService(normalizeBaseUrl(newAddress))
        return currentApiService!!
    }

    /**
     * Создание экземпляра API сервиса для указанного адреса
     *
     * Создает и настраивает Retrofit клиент с заданным базовым URL,
     * HTTP клиентом и конвертером Gson.
     *
     * @param baseUrl Базовый URL сервера (должен заканчиваться на "/")
     * @return Сконфигурированный экземпляр [MonitorApiService]
     *
     * @throws IllegalArgumentException если базовый URL некорректен
     */
    private fun createApiService(baseUrl: String): MonitorApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(MonitorApiService::class.java)
    }

    /**
     * Получение текущего сохраненного адреса сервера
     *
     * Читает адрес сервера из SharedPreferences. Если адрес не сохранен,
     * возвращает адрес сервера по умолчанию.
     *
     * @return Текущий адрес сервера или адрес по умолчанию
     */
    fun getCurrentServerAddress(): String {
        return normalizeBaseUrl(
            prefs.getString("server_address", DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        )
    }

    /**
     * Сохранение адреса сервера в SharedPreferences
     *
     * @param address Адрес сервера для сохранения
     */
    private fun saveServerAddress(address: String) {
        prefs.edit().putString("server_address", normalizeBaseUrl(address)).apply()
    }

    private fun normalizeBaseUrl(address: String): String {
        val trimmed = address.trim()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    companion object {
        private const val DEFAULT_BASE_URL = "http://10.240.68.80:5000/"
    }
}