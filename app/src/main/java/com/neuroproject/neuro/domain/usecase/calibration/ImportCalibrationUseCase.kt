
package com.neuroproject.neuro.domain.usecase.calibration

import com.neuroproject.neuro.domain.model.CalibrationSample
import com.neuroproject.neuro.domain.repository.CalibrationRepository
import com.neuroproject.neuro.domain.repository.DeviceGateway
import com.neuroproject.neuro.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case импорта калибровки в устройство.
 *
 * При выборе "использовать прошлую калибровку" загружает сохранённые
 * параметры из БД и импортирует их в Capsule. Включает:
 * - основную калибровку ЭЭГ (частоты, полосы пропускания)
 * - калибровку продуктивности (если данные доступны)
 * - калибровку физиологии (если данные доступны)
 *
 * Если прошлая калибровка отсутствует или выбрана новая — запускает
 * процесс калибровки через [DeviceGateway.startSignalAndHR].
 *
 * @param deviceGateway шлюз устройства для импорта параметров
 * @param calibrationRepository репозиторий хранения калибровки
 * @param authRepository репозиторий аутентификации (для получения userId)
 */
class ImportCalibrationUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway,
    private val calibrationRepository: CalibrationRepository,
    private val authRepository: AuthRepository
) {
    /**
     * Выполняет импорт калибровки.
     *
     * @param usePrevious true — загрузить прошлую калибровку из БД,
     *                    false — запустить новую калибровку.
     */
    suspend operator fun invoke(usePrevious: Boolean) {
        if (usePrevious) {
            val userId = authRepository.getUserId()
            val previousData = calibrationRepository.getPreviousCalibration(userId)
            if (previousData != null) {
                deviceGateway.importCalibration(previousData)

                previousData.let { data ->
                    if (data.productivityGravity != null) {
                        deviceGateway.importProductivityCalibration(
                            gravity = data.productivityGravity,
                            productivity = data.productivityProductivity ?: 0f,
                            fatigue = data.productivityFatigue ?: 0f,
                            reverseFatigue = data.productivityReverseFatigue ?: 0f,
                            relaxation = data.productivityRelaxation ?: 0f,
                            concentration = data.productivityConcentration ?: 0f
                        )
                    }
                    if (data.physiologicalAlpha != null) {
                        deviceGateway.importPhysiologicalCalibration(
                            alpha = data.physiologicalAlpha,
                            beta = data.physiologicalBeta ?: 0f,
                            alphaGravity = data.physiologicalAlphaGravity ?: 0f,
                            betaGravity = data.physiologicalBetaGravity ?: 0f,
                            concentration = data.physiologicalConcentration ?: 0f
                        )
                    }
                }
                return
            }
        }
        // Иначе запускаем новую калибровку
        deviceGateway.startSignalAndHR()
    }
}