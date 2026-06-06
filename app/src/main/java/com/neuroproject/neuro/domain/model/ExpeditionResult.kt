package com.neuroproject.neuro.domain.model

sealed class ExpeditionResult {
    data class Success(val expeditionId: String) : ExpeditionResult()
    object NotSet : ExpeditionResult()
    data class Error(val message: String) : ExpeditionResult()
}