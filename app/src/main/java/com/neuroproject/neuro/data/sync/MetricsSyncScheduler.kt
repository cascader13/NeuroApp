package com.neuroproject.neuro.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Планировщик фоновой синхронизации метрик через WorkManager.
 *
 * Управляет одноразовой и периодической синхронизацией данных с сервером.
 * Гарантирует выполнение только при наличии сети и достаточном заряде батареи.
 *
 * Основные возможности:
 * - Одноразовая синхронизация (ручной запуск)
 * - Периодическая синхронизация (автоматический запуск)
 * - Наблюдение за состоянием синхронизации
 * - Отмена запланированных задач
 *
 * @see MetricsSyncWorker
 * @see SyncState
 */
@Singleton
class MetricsSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** WorkManager для управления фоновыми задачами */
    private val workManager: WorkManager
        get() = WorkManager.getInstance(context)

    /**
     * Запустить одноразовую фоновую синхронизацию.
     *
     * @param batchSize Размер пакета для отправки
     * @param replaceExisting Заменить ли существующую задачу
     */
    fun enqueueManualBackgroundSync(
        batchSize: Int = DEFAULT_BATCH_SIZE,
        replaceExisting: Boolean = false
    ) {
        val request = OneTimeWorkRequestBuilder<MetricsSyncWorker>()
            .setConstraints(syncConstraints())
            .setInputData(
                workDataOf(
                    MetricsSyncWorker.KEY_BATCH_SIZE to batchSize,
                    MetricsSyncWorker.KEY_STOP_ON_ERROR to false
                )
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_MINUTES, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniqueWork(
            MetricsSyncWorker.UNIQUE_ONE_TIME_WORK_NAME,
            if (replaceExisting) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Включить периодическую синхронизацию.
     *
     * @param repeatIntervalMinutes Интервал повторения (мин), минимум 15
     * @param batchSize Размер пакета для отправки
     */
    fun enablePeriodicSync(
        repeatIntervalMinutes: Long = DEFAULT_REPEAT_MINUTES,
        batchSize: Int = DEFAULT_BATCH_SIZE
    ) {
        val interval = repeatIntervalMinutes.coerceAtLeast(MIN_PERIODIC_INTERVAL_MINUTES)

        val request = PeriodicWorkRequestBuilder<MetricsSyncWorker>(interval, TimeUnit.MINUTES)
            .setConstraints(syncConstraints())
            .setInputData(
                workDataOf(
                    MetricsSyncWorker.KEY_BATCH_SIZE to batchSize,
                    MetricsSyncWorker.KEY_STOP_ON_ERROR to false
                )
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_MINUTES, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            MetricsSyncWorker.UNIQUE_PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /** Отключить периодическую синхронизацию */
    fun disablePeriodicSync() {
        workManager.cancelUniqueWork(MetricsSyncWorker.UNIQUE_PERIODIC_WORK_NAME)
    }

    /** Отменить одноразовую синхронизацию */
    fun cancelManualBackgroundSync() {
        workManager.cancelUniqueWork(MetricsSyncWorker.UNIQUE_ONE_TIME_WORK_NAME)
    }

    /**
     * Наблюдать за состоянием синхронизации.
     *
     * @return Flow с текущим состоянием (Idle, Enqueued, Running)
     */
    fun observeSyncState(): Flow<SyncState> {
        val periodicWork = workManager.getWorkInfosForUniqueWorkFlow(
            MetricsSyncWorker.UNIQUE_PERIODIC_WORK_NAME
        )
        val oneTimeWork = workManager.getWorkInfosForUniqueWorkFlow(
            MetricsSyncWorker.UNIQUE_ONE_TIME_WORK_NAME
        )

        return combine(periodicWork, oneTimeWork) { periodic, oneTime ->
            val isPeriodicRunning = periodic.any { it.state == WorkInfo.State.RUNNING }
            val isOneTimeRunning = oneTime.any { it.state == WorkInfo.State.RUNNING }
            val isEnqueued = periodic.any { it.state == WorkInfo.State.ENQUEUED } ||
                    oneTime.any { it.state == WorkInfo.State.ENQUEUED }

            when {
                isPeriodicRunning || isOneTimeRunning -> SyncState.Running
                isEnqueued -> SyncState.Enqueued
                else -> SyncState.Idle
            }
        }
    }

    /**
     * Создать ограничения для синхронизации.
     * Требуется подключение к сети и достаточный заряд батареи.
     */
    private fun syncConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()
    }

    companion object {
        private const val DEFAULT_BATCH_SIZE = 100
        private const val DEFAULT_REPEAT_MINUTES = 60L
        private const val MIN_PERIODIC_INTERVAL_MINUTES = 15L
        private const val BACKOFF_MINUTES = 5L
    }
}

/**
 * Состояние синхронизации.
 */
enum class SyncState {
    /** Синхронизация не выполняется */
    Idle,
    /** Синхронизация в очереди на выполнение */
    Enqueued,
    /** Синхронизация выполняется */
    Running
}
