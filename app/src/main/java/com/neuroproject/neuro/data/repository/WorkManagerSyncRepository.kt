package com.neuroproject.neuro.data.repository

import com.neuroproject.neuro.data.sync.MetricsSyncScheduler
import com.neuroproject.neuro.domain.repository.SyncRepository
import com.neuroproject.neuro.domain.repository.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
/**
 * Репозиторий синхронизации данных через WorkManager.
 */
class WorkManagerSyncRepository @Inject constructor(
    private val syncScheduler: MetricsSyncScheduler
) : SyncRepository {

    override fun observeSyncState(): Flow<SyncState> {
        return syncScheduler.observeSyncState().map { schedulerState ->
            when (schedulerState) {
                com.neuroproject.neuro.data.sync.SyncState.Idle -> SyncState.Idle
                com.neuroproject.neuro.data.sync.SyncState.Enqueued -> SyncState.Enqueued
                com.neuroproject.neuro.data.sync.SyncState.Running -> SyncState.Running
            }
        }
    }

    override fun enablePeriodicSync() {
        syncScheduler.enablePeriodicSync()
    }

    override fun disablePeriodicSync() {
        syncScheduler.disablePeriodicSync()
    }

    override fun enqueueManualSync() {
        syncScheduler.enqueueManualBackgroundSync()
    }

    override fun cancelManualSync() {
        syncScheduler.cancelManualBackgroundSync()
    }
}
