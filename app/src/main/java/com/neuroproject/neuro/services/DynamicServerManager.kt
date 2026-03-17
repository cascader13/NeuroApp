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

@Singleton
class DynamicServerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @SecondServer private val gson: Gson,
    @SecondServer private val okHttpClient: OkHttpClient
) {
    private val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    private var currentApiService: MonitorApiService? = null

    fun getApiService(): MonitorApiService {
        val serverAddress = getCurrentServerAddress()

        if (currentApiService == null) {
            currentApiService = createApiService(serverAddress)
        }

        return currentApiService!!
    }

    fun updateServerAddress(newAddress: String): MonitorApiService {
        saveServerAddress(newAddress)
        currentApiService = createApiService(newAddress)
        return currentApiService!!
    }

    private fun createApiService(baseUrl: String): MonitorApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(MonitorApiService::class.java)
    }

    fun getCurrentServerAddress(): String {
        return prefs.getString("server_address", "http://10.240.68.80:5000")
            ?: "http://10.240.68.80:5000"
    }

    private fun saveServerAddress(address: String) {
        prefs.edit().putString("server_address", address).apply()
    }
}