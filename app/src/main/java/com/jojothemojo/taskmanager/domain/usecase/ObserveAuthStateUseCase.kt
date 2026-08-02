package com.jojothemojo.taskmanager.domain.usecase

import com.jojothemojo.taskmanager.domain.model.AuthState
import com.jojothemojo.taskmanager.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<AuthState> = authRepository.observeAuthState()
}
