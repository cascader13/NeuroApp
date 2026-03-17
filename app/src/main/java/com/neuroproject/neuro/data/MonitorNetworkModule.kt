package com.neuroproject.neuro

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.neuroproject.neuro.data.remote.MonitorApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MonitorNetworkModule {

    private const val DEFAULT_BASE_URL = "http://10.240.68.80:5000"

    @Provides
    @Singleton
    fun provideServerAddressPreferences(@ApplicationContext context: Context): ServerAddressPreferences {
        return ServerAddressPreferences(context)
    }

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
    fun provideMonitorApiService(
        @SecondServer okHttpClient: OkHttpClient,
        @SecondServer gson: Gson,
        serverAddressPreferences: ServerAddressPreferences
    ): MonitorApiService {
        val baseUrl = serverAddressPreferences.getServerAddress()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(MonitorApiService::class.java)
    }
}

// Класс для работы с адресом сервера в SharedPreferences
class ServerAddressPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    fun getServerAddress(): String {
        return prefs.getString("server_address", "http://10.240.68.80:5000") ?: "http://10.240.68.80:5000"
    }
}