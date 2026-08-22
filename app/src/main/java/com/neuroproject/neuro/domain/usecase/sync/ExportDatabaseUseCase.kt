package com.neuroproject.neuro.domain.usecase.sync

import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.repository.DatabaseExportRepository
import javax.inject.Inject

/**
 * Экспортирует базу данных в папку загрузок устройства.
 *
 * @return [Result.Success] с путём к файлу, [Result.Error] при ошибке экспорта
 */
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
