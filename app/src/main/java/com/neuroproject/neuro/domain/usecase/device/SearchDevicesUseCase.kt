package com.neuroproject.neuro.domain.usecase.device

import com.neuroproject.neuro.domain.model.DeviceInfo
import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchDevicesUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    operator fun invoke(): Flow<List<DeviceInfo>> = deviceGateway.searchDevices()
}