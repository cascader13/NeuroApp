package com.neuroproject.neuro.domain.usecase.sensor

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.ResistanceData
import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ObserveResistanceUseCaseTest {

    private lateinit var useCase: ObserveResistanceUseCase
    private val deviceGateway: DeviceGateway = mock()

    @Before
    fun setup() {
        useCase = ObserveResistanceUseCase(deviceGateway)
    }

    @Test
    fun `invoke returns flow from deviceGateway`() = runTest {
        val data = ResistanceData(o1 = 500.0, o2 = 600.0, t3 = 700.0, t4 = 800.0)
        whenever(deviceGateway.observeResistance()).thenReturn(flowOf(data))

        val result = useCase().first()
        assertThat(result).isEqualTo(data)
    }
}
