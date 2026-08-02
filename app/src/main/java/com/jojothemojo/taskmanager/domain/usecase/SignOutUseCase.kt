package com.jojothemojo.taskmanager.domain.usecase

import com.jojothemojo.taskmanager.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() = authRepository.signOut()
}
