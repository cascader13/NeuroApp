package com.neuroproject.neuro.data.remote

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class StringRequest(
    @SerializedName("id")
    val id: String?,

    @SerializedName("text")
    val text: String
)

data class StringResponse(
    @SerializedName("success")
    val success: Boolean
)