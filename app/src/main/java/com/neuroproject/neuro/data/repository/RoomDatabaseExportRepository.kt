package com.neuroproject.neuro.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.repository.DatabaseExportRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomDatabaseExportRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : DatabaseExportRepository {

    override suspend fun exportDatabaseToDownloads(): Result<String> {
        return try {
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            if (!dbFile.exists()) {
                return Result.Error(
                    Exception("Database not found"),
                    "Файл базы данных не найден"
                )
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "$DATABASE_EXPORT_PREFIX$timestamp.db"

            val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportWithMediaStore(dbFile, fileName)
            } else {
                exportWithDirectFile(dbFile, fileName)
            }

            Result.Success(message)
        } catch (e: Exception) {
            Result.Error(e, "Ошибка экспорта: ${e.message}")
        }
    }

    private fun exportWithMediaStore(dbFile: File, fileName: String): String {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return "Не удалось создать файл в Downloads"

        resolver.openOutputStream(uri)?.use { output ->
            dbFile.inputStream().use { input ->
                input.copyTo(output)
            }
        }

        return "База данных экспортирована в Downloads/$fileName"
    }

    private fun exportWithDirectFile(dbFile: File, fileName: String): String {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        val outputFile = File(downloadsDir, fileName)
        dbFile.copyTo(outputFile, overwrite = true)
        return "База данных экспортирована в ${outputFile.absolutePath}"
    }

    companion object {
        private const val DATABASE_NAME = "metrics_database_v7"
        private const val DATABASE_EXPORT_PREFIX = "neuro_database_"
    }
}
