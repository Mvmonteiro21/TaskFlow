package com.example.taskflow.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskflow.di.AppContainer
import com.example.taskflow.domain.model.Task
import com.example.taskflow.ui.settings.SettingsScreen
import com.example.taskflow.ui.settings.SettingsViewModel
import com.example.taskflow.ui.taskform.TaskFormScreen
import com.example.taskflow.ui.taskform.TaskFormViewModel
import com.example.taskflow.ui.tasklist.TaskListScreen
import com.example.taskflow.ui.tasklist.TaskListViewModel

object Routes {
    const val LIST = "tasks"
    const val SETTINGS = "settings"
    const val ARG_TASK_ID = "taskId"
    const val FORM = "form/{$ARG_TASK_ID}"
    fun form(taskId: Long) = "form/$taskId"
}

@Composable
fun AppNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            val vm: TaskListViewModel = viewModel(factory = TaskListViewModel.factory(container))
            TaskListScreen(
                viewModel = vm,
                onAddTask = { navController.navigate(Routes.form(Task.NEW_ID)) },
                onEditTask = { id -> navController.navigate(Routes.form(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route = Routes.FORM,
            arguments = listOf(navArgument(Routes.ARG_TASK_ID) { type = NavType.LongType })
        ) { entry ->
            val taskId = entry.arguments?.getLong(Routes.ARG_TASK_ID) ?: Task.NEW_ID
            val vm: TaskFormViewModel = viewModel(factory = TaskFormViewModel.factory(container, taskId))
            TaskFormScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
            SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
