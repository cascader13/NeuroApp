package com.neuroproject.neuro

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.neuroproject.neuro.data.remote.MonitorApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MonitorNetworkModule {

    private const val SECOND_BASE_URL = "http://192.168.0.200:5000"

    @Provides
    @Singleton
    @SecondServer
    fun provideSecondGson(): Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    @Provides
    @Singleton
    @SecondServer
    fun provideSecondHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    @Singleton
    @SecondServer
    fun provideSecondOkHttpClient(
        @SecondServer loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @Singleton
    @SecondServer
    fun provideSecondRetrofit(
        @SecondServer okHttpClient: OkHttpClient,
        @SecondServer gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl(SECOND_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // ← Самое важное
    @Provides
    @Singleton
    fun provideMonitorApiService(
        @SecondServer retrofit: Retrofit
    ): MonitorApiService = retrofit.create(MonitorApiService::class.java)
}