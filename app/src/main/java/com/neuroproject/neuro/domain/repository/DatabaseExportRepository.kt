package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.Result

interface DatabaseExportRepository {
    suspend fun exportDatabaseToDownloads(): Result<String>
}
