package com.neuroproject.neuro.domain.usecase.sensor

import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class StartResistanceCheckUseCaseTest {

    private lateinit var useCase: StartResistanceCheckUseCase
    private val deviceGateway: DeviceGateway = mock()

    @Before
    fun setup() {
        useCase = StartResistanceCheckUseCase(deviceGateway)
    }

    @Test
    fun `invoke calls startResistanceCheck on deviceGateway`() = runTest {
        useCase()
        verify(deviceGateway).startResistanceCheck()
    }
}
