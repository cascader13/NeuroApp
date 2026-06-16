package com.neuroproject.neuro.domain.usecase.recording

import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class StartRecordingUseCaseTest {

    private lateinit var useCase: StartRecordingUseCase
    private val deviceGateway: DeviceGateway = mock()

    @Before
    fun setup() {
        useCase = StartRecordingUseCase(deviceGateway)
    }

    @Test
    fun `invoke calls startSession on deviceGateway`() = runTest {
        useCase()
        verify(deviceGateway).startSession()
    }
}
