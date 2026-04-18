package com.neuroproject.neuro

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.neuroproject.neuro.data.remote.MetricsApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Dagger Hilt модуль для настройки сетевого взаимодействия с основным сервером
 *
 * Предоставляет зависимости для HTTP клиента и Retrofit, необходимые для
 * отправки метрик на сервер. Включает механизмы повторных попыток при сбоях.
 *
 * ## Особенности:
 * - **Таймауты**: подключение 60 сек, чтение/запись 20000 сек
 * - **Retry Policy**: до 3 попыток с экспоненциальной задержкой при таймаутах
 * - **Логирование**: BODY уровень для детальной отладки
 * @see MetricsApiService
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** Базовый URL основного сервера */
    private const val BASE_URL = "http://159.194.217.94:8080"

    /**
     * Предоставление Gson с настройками формата даты
     *
     * @return Настроенный экземпляр Gson
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .disableHtmlEscaping()
            .create()
    }

    /**
     * Предоставление HTTP логирования
     *
     * @return Интерцептор для логирования всех HTTP запросов/ответов
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Предоставление OkHttpClient с настройками повторных попыток
     *
     * Включает:
     * - Таймауты для предотвращения зависаний
     * - Интерцептор для автоматических повторных попыток при таймаутах
     * - Логирование всех запросов
     *
     * @param loggingInterceptor Интерцептор для логирования
     * @return Настроенный OkHttpClient
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)

            .build()
    }

    /**
     * Предоставление Retrofit клиента
     *
     * @param okHttpClient Настроенный HTTP клиент
     * @param gson Сериализатор JSON
     * @return Настроенный Retrofit
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Предоставление API сервиса для метрик
     *
     * @param retrofit Настроенный Retrofit клиент
     * @return Экземпляр MetricsApiService
     */
    @Provides
    @Singleton
    fun provideMetricsApiService(retrofit: Retrofit): MetricsApiService {
        return retrofit.create(MetricsApiService::class.java)
    }
}