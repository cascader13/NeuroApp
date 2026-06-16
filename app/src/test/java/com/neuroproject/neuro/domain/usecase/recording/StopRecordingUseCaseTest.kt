package com.neuroproject.neuro.domain.usecase.recording

import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class StopRecordingUseCaseTest {

    private lateinit var useCase: StopRecordingUseCase
    private val deviceGateway: DeviceGateway = mock()

    @Before
    fun setup() {
        useCase = StopRecordingUseCase(deviceGateway)
    }

    @Test
    fun `invoke calls stopSession on deviceGateway`() = runTest {
        useCase()
        verify(deviceGateway).stopSession()
    }
}
