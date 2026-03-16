package com.neuroproject.neuro.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MonitorApiService {
    @POST("/api/sendString")  // Замените на реальный endpoint
    suspend fun sendString(
        @Body request: StringRequest
    ): Response<StringResponse>
}