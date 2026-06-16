package com.neuroproject.neuro.domain.usecase.sync

import com.neuroproject.neuro.domain.repository.SyncRepository
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class EnablePeriodicSyncUseCaseTest {

    private lateinit var useCase: EnablePeriodicSyncUseCase
    private val syncRepository: SyncRepository = mock()

    @Before
    fun setup() {
        useCase = EnablePeriodicSyncUseCase(syncRepository)
    }

    @Test
    fun `when invoke then calls repository enablePeriodicSync`() {
        useCase()

        org.mockito.kotlin.verify(syncRepository).enablePeriodicSync()
    }
}
