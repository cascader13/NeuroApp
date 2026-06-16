package com.neuroproject.neuro.domain.usecase.sync

import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.repository.DatabaseExportRepository
import javax.inject.Inject

class ExportDatabaseUseCase @Inject constructor(
    private val databaseExportRepository: DatabaseExportRepository
) {
    suspend operator fun invoke(): Result<String> {
        return try {
            databaseExportRepository.exportDatabaseToDownloads()
        } catch (e: Exception) {
            Result.Error(e, "Ошибка экспорта: ${e.message}")
        }
    }
}
