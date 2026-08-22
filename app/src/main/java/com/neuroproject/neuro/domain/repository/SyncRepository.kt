package com.neuroproject.neuro.domain.repository

import kotlinx.coroutines.flow.Flow

enum class SyncState {
    Idle,
    Enqueued,
    Running
}

interface SyncRepository {
    fun observeSyncState(): Flow<SyncState>
    fun enablePeriodicSync()
    fun disablePeriodicSync()
    fun enqueueManualSync()
    fun cancelManualSync()
}
