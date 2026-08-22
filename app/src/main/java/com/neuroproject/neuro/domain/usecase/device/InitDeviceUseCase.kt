package com.neuroproject.neuro.domain.usecase.device

import com.neuroproject.neuro.domain.repository.DeviceGateway
import javax.inject.Inject

/**
 * Инициализирует подсистему работы с устройством.
 */
class InitDeviceUseCase  @Inject constructor(
    private val deviceGateway: DeviceGateway
){
    operator fun invoke(){
        deviceGateway.init()
    }
}