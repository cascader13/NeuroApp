package com.neuroproject.neuro.domain.usecase.sensor

import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class StopResistanceCheckUseCaseTest {

    private lateinit var useCase: StopResistanceCheckUseCase
    private val deviceGateway: DeviceGateway = mock()

    @Before
    fun setup() {
        useCase = StopResistanceCheckUseCase(deviceGateway)
    }

    @Test
    fun `invoke calls stopResistanceCheck on deviceGateway`() = runTest {
        useCase()
        verify(deviceGateway).stopResistanceCheck()
    }
}
