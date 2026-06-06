package com.neuroproject.neuro.domain.usecase.recording

import com.neuroproject.neuro.domain.repository.DeviceGateway
import javax.inject.Inject

class StartRecordingUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    suspend operator fun invoke() {
        deviceGateway.startSession()
    }
}