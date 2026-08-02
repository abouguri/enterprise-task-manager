package com.jojothemojo.taskmanager.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojothemojo.taskmanager.domain.model.AuthState
import com.jojothemojo.taskmanager.domain.usecase.ObserveAuthStateUseCase
import com.jojothemojo.taskmanager.domain.usecase.SignInUseCase
import com.jojothemojo.taskmanager.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    val authState: StateFlow<AuthState> = observeAuthStateUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Loading)

    fun signIn(activity: Activity) {
        viewModelScope.launch { signInUseCase(activity) }
    }

    fun signOut() {
        viewModelScope.launch { signOutUseCase() }
    }
}
