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

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://192.168.3.54:8080"

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            // Всегда показываем логи для разработки
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // Увеличьте таймауты
            .readTimeout(20000, TimeUnit.SECONDS)
            .writeTimeout(20000, TimeUnit.SECONDS)
            .callTimeout(20000, TimeUnit.SECONDS) // Общий таймаут вызова
            .addInterceptor(loggingInterceptor)
            // Добавьте retry при таймаутах
            .addInterceptor { chain ->
                val request = chain.request()
                var response: Response? = null
                var lastException: IOException? = null

                // 3 попытки с увеличением задержки
                for (attempt in 1..3) {
                    try {
                        response = chain.proceed(request)
                        if (response.isSuccessful) {
                            return@addInterceptor response
                        }
                    } catch (e: SocketTimeoutException) {
                        lastException = e
                        if (attempt < 3) {
                            Thread.sleep(1000L * attempt) // Экспоненциальная backoff
                        }
                    } catch (e: IOException) {
                        lastException = e
                        break
                    }
                }

                throw lastException ?: IOException("Request failed")
            }
            .build()
    }


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

    @Provides
    @Singleton
    fun provideMetricsApiService(retrofit: Retrofit): MetricsApiService {
        return retrofit.create(MetricsApiService::class.java)
    }


}