package com.neuroproject.neuro.domain.usecase.device

import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ConnectDeviceUseCaseTest {

    private lateinit var useCase: ConnectDeviceUseCase
    private val deviceGateway: DeviceGateway = mock()

    @Before
    fun setup() {
        useCase = ConnectDeviceUseCase(deviceGateway)
    }

    @Test
    fun `invoke calls connect with deviceId`() = runTest {
        useCase("device-123")
        verify(deviceGateway).connect("device-123")
    }
}
