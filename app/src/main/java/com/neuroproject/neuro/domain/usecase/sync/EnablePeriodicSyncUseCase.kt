package com.neuroproject.neuro.domain.usecase.sync

import com.neuroproject.neuro.domain.repository.SyncRepository
import javax.inject.Inject

/**
 * Включает периодическую синхронизацию данных с сервером.
 */
class EnablePeriodicSyncUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    operator fun invoke() {
        syncRepository.enablePeriodicSync()
    }
}
