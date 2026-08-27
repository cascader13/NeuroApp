package com.neuroproject.neuro.domain.usecase.sensor

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.BatteryData
import com.neuroproject.neuro.domain.repository.CapsuleDeviceGateway
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ObserveBatteryUseCaseTest {

    private lateinit var useCase: ObserveBatteryUseCase
    private val deviceGateway: CapsuleDeviceGateway = mock()

    @Before
    fun setup() {
        useCase = ObserveBatteryUseCase(deviceGateway)
    }

    @Test
    fun `invoke returns flow from deviceGateway`() = runTest {
        val data = BatteryData(chargePercent = 85f)
        whenever(deviceGateway.observeBatteryCharge()).thenReturn(flowOf(data))

        val result = useCase().first()
        assertThat(result).isEqualTo(data)
    }
}
