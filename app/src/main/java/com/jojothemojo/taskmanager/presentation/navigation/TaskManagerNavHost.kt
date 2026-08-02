package com.jojothemojo.taskmanager.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jojothemojo.taskmanager.domain.model.AuthState
import com.jojothemojo.taskmanager.presentation.ui.LoginScreen
import com.jojothemojo.taskmanager.presentation.ui.TaskListScreen
import com.jojothemojo.taskmanager.presentation.viewmodel.AuthViewModel
import com.jojothemojo.taskmanager.presentation.viewmodel.TaskListViewModel

object TaskManagerDestinations {
    const val LOGIN = "login"
    const val TASK_LIST = "task_list"
}

@Composable
fun TaskManagerNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = TaskManagerDestinations.LOGIN,
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> navController.navigate(TaskManagerDestinations.TASK_LIST) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
            is AuthState.Unauthenticated -> navController.navigate(TaskManagerDestinations.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
            else -> Unit // Loading / Error: stay put, LoginScreen renders the state itself.
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(TaskManagerDestinations.LOGIN) {
            LoginScreen(
                authState = authState,
                onSignInClick = authViewModel::signIn,
            )
        }
        composable(TaskManagerDestinations.TASK_LIST) {
            val taskListViewModel: TaskListViewModel = hiltViewModel()
            val tasks by taskListViewModel.tasks.collectAsStateWithLifecycle()
            TaskListScreen(
                tasks = tasks,
                onAddTask = taskListViewModel::addTask,
                onToggleTask = taskListViewModel::toggleCompleted,
                onDeleteTask = taskListViewModel::deleteTask,
                onSignOutClick = authViewModel::signOut,
            )
        }
    }
}
