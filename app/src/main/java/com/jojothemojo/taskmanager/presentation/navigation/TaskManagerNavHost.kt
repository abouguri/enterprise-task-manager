package com.jojothemojo.taskmanager.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jojothemojo.taskmanager.presentation.ui.LoginScreen
import com.jojothemojo.taskmanager.presentation.ui.TaskListScreen

object TaskManagerDestinations {
    const val LOGIN = "login"
    const val TASK_LIST = "task_list"
}

@Composable
fun TaskManagerNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = TaskManagerDestinations.LOGIN,
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(TaskManagerDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(TaskManagerDestinations.TASK_LIST) {
                        popUpTo(TaskManagerDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(TaskManagerDestinations.TASK_LIST) {
            TaskListScreen()
        }
    }
}
