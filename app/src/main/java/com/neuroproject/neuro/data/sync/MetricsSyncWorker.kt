package com.neuroproject.neuro.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.neuroproject.neuro.data.BatchUploadProgress
import com.neuroproject.neuro.data.MetricsUploadRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.CancellationException

/**
 * Фоновая синхронизация метрик через WorkManager.
 *
 * Ручная синхронизация остаётся прежней: UI может напрямую вызывать
 * MetricsUploadRepository.uploadInBatches(). Worker использует тот же репозиторий,
 * поэтому формат JSON, пакетирование и пометка isMarked совпадают в обоих сценариях.
 */
@HiltWorker
class MetricsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val uploadRepository: MetricsUploadRepository,
    private val notificationHelper: SyncNotificationHelper
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        setForeground(notificationHelper.createForegroundInfo())
        val batchSize = inputData.getInt(KEY_BATCH_SIZE, DEFAULT_BATCH_SIZE)
            .coerceIn(MIN_BATCH_SIZE, MAX_BATCH_SIZE)
        val stopOnError = inputData.getBoolean(KEY_STOP_ON_ERROR, false)

        return try {
            var terminalProgress: BatchUploadProgress? = null
            var totalRecords = 0

            uploadRepository.uploadInBatches(
                batchSize = batchSize,
                enableRetry = true,
                stopOnError = stopOnError
            ).collect { progress ->
                terminalProgress = progress
                if (progress is BatchUploadProgress.BatchesCreated) {
                    totalRecords = progress.totalRecords
                }
                if (progress is BatchUploadProgress.BatchCompleted) {
                    setForeground(
                        notificationHelper.createForegroundInfo(
                            progress.totalSentSoFar,
                            totalRecords
                        )
                    )
                }
            }

            terminalProgress?.let { notificationHelper.showSyncResult(it) }

            when (terminalProgress) {
                is BatchUploadProgress.Completed,
                BatchUploadProgress.NoData -> Result.success()

                is BatchUploadProgress.PartialSuccess,
                is BatchUploadProgress.Stopped,
                is BatchUploadProgress.Error -> retryOrFailure()

                else -> retryOrFailure()
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            retryOrFailure()
        }
    }

    private fun retryOrFailure(): Result {
        return if (runAttemptCount < MAX_WORK_RETRIES) {
            Result.retry()
        } else {
            Result.failure()
        }
    }

    companion object {
        const val UNIQUE_ONE_TIME_WORK_NAME = "metrics_sync_once"
        const val UNIQUE_PERIODIC_WORK_NAME = "metrics_sync_periodic"
        const val KEY_BATCH_SIZE = "batch_size"
        const val KEY_STOP_ON_ERROR = "stop_on_error"

        private const val DEFAULT_BATCH_SIZE = 100
        private const val MIN_BATCH_SIZE = 1
        private const val MAX_BATCH_SIZE = 500
        private const val MAX_WORK_RETRIES = 3
    }
}
