package com.neuroproject.neuro.domain.usecase.device

import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DisconnectDeviceUseCaseTest {

    private lateinit var useCase: DisconnectDeviceUseCase
    private val deviceGateway: DeviceGateway = mock()

    @Before
    fun setup() {
        useCase = DisconnectDeviceUseCase(deviceGateway)
    }

    @Test
    fun `invoke calls disconnect on deviceGateway`() = runTest {
        useCase()
        verify(deviceGateway).disconnect()
    }
}
