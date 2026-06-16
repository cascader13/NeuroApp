package com.neuroproject.neuro.domain.usecase.sync

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.repository.SyncRepository
import com.neuroproject.neuro.domain.repository.SyncState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ObserveSyncStateUseCaseTest {

    private lateinit var useCase: ObserveSyncStateUseCase
    private val syncRepository: SyncRepository = mock()

    @Before
    fun setup() {
        useCase = ObserveSyncStateUseCase(syncRepository)
    }

    @Test
    fun `when repository returns Idle then use case returns Idle`() = runTest {
        whenever(syncRepository.observeSyncState()).thenReturn(flowOf(SyncState.Idle))

        val result = useCase().first()

        assertThat(result).isEqualTo(SyncState.Idle)
    }

    @Test
    fun `when repository returns Running then use case returns Running`() = runTest {
        whenever(syncRepository.observeSyncState()).thenReturn(flowOf(SyncState.Running))

        val result = useCase().first()

        assertThat(result).isEqualTo(SyncState.Running)
    }

    @Test
    fun `when repository returns Enqueued then use case returns Enqueued`() = runTest {
        whenever(syncRepository.observeSyncState()).thenReturn(flowOf(SyncState.Enqueued))

        val result = useCase().first()

        assertThat(result).isEqualTo(SyncState.Enqueued)
    }

    @Test
    fun `use case delegates to repository observeSyncState`() = runTest {
        whenever(syncRepository.observeSyncState()).thenReturn(flowOf(SyncState.Idle))

        useCase().first()

        org.mockito.kotlin.verify(syncRepository).observeSyncState()
    }
}
