package com.neuroproject.neuro.domain.usecase.sync

import com.neuroproject.neuro.domain.repository.SyncRepository
import javax.inject.Inject

class EnablePeriodicSyncUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    operator fun invoke() {
        syncRepository.enablePeriodicSync()
    }
}
