package com.neuroproject.neuro.domain.usecase.device

import com.neuroproject.neuro.domain.repository.DeviceGateway
import javax.inject.Inject

class InitDeviceUseCase  @Inject constructor(
    private val deviceGateway: DeviceGateway
){
    operator fun invoke(){
        deviceGateway.init()
    }
}