package com.neuroproject.neuro.domain.usecase

import com.neuroproject.neuro.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Сохраняет идентификатор пользователя через доменный контракт AuthRepository.
 *
 * UseCase больше не зависит от Android Context/SharedPreferences, поэтому domain-слой
 * остается чистым и легко тестируется обычными unit-тестами.
 */
class SaveMobileIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(mobileId: String) {
        authRepository.saveUserId(mobileId)
    }
}
