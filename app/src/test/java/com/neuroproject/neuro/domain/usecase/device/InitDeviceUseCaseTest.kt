package com.neuroproject.neuro.domain.usecase.device

import com.neuroproject.neuro.domain.repository.DeviceGateway
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class InitDeviceUseCaseTest {

    private lateinit var useCase: InitDeviceUseCase
    private val deviceGateway: DeviceGateway = mock()

    @Before
    fun setup() {
        useCase = InitDeviceUseCase(deviceGateway)
    }

    @Test
    fun `invoke calls init on deviceGateway`() {
        useCase()
        verify(deviceGateway).init()
    }
}
