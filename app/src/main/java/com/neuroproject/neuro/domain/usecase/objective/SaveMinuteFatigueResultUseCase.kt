package com.neuroproject.neuro.domain.usecase.objective

import com.neuroproject.neuro.domain.model.FatigueResult
import com.neuroproject.neuro.domain.repository.ObjectiveMetricsRepository
import javax.inject.Inject

/**
 * Сохраняет результат расчёта утомления за минуту.
 *
 * @param result результат утомления для сохранения
 */
class SaveMinuteFatigueResultUseCase @Inject constructor(
    private val repository: ObjectiveMetricsRepository
) {
    suspend operator fun invoke(result: FatigueResult) {
        repository.saveMinuteFatigueResult(result)
    }
}