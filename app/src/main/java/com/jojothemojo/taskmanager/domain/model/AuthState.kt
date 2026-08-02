package com.jojothemojo.taskmanager.domain.model

sealed interface AuthState {
    data class Authenticated(val user: User) : AuthState
    data object Unauthenticated : AuthState
    data object Loading : AuthState
    data class Error(val message: String) : AuthState
}
