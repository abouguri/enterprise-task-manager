package com.jojothemojo.taskmanager.presentation.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jojothemojo.taskmanager.domain.model.AuthState

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    authState: AuthState = AuthState.Unauthenticated,
    onSignInClick: (Activity) -> Unit = {},
) {
    val activity = LocalContext.current as Activity

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Login")
        when (authState) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Error -> {
                Text(text = authState.message)
                Button(onClick = { onSignInClick(activity) }) {
                    Text(text = "Sign in")
                }
            }
            else -> {
                Button(onClick = { onSignInClick(activity) }) {
                    Text(text = "Sign in")
                }
            }
        }
    }
}
