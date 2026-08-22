package com.neuroproject.neuro.domain.usecase.sync

import com.neuroproject.neuro.domain.repository.SyncRepository
import com.neuroproject.neuro.domain.repository.SyncState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Наблюдает за состоянием синхронизации данных.
 *
 * @return [Flow] с текущим состоянием синхронизации
 */
class ObserveSyncStateUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    operator fun invoke(): Flow<SyncState> {
        return syncRepository.observeSyncState()
    }
}
