package com.neuroproject.neuro.data.remote

import com.neuroproject.neuro.data.remote.UploadRequest
import com.neuroproject.neuro.data.remote.UploadResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MetricsApiService {
    @POST("/api/metrics/upload")
    suspend fun uploadMetrics(
        @Body request: UploadRequest
    ): Response<UploadResponse>
}

data class UploadResponse(
    val result: Boolean
)