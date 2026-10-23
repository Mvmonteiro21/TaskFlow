package com.example.taskflow.di

import android.content.Context
import com.example.taskflow.data.local.AppDatabase
import com.example.taskflow.data.preferences.SharedPreferencesSettingsRepository
import com.example.taskflow.data.repository.TaskRepositoryImpl
import com.example.taskflow.domain.repository.SettingsRepository
import com.example.taskflow.domain.repository.TaskRepository
import com.example.taskflow.domain.usecase.*

/** Injeção de dependência manual: único lugar que conhece as implementações concretas. */
class AppContainer(context: Context) {
    private val database = AppDatabase.create(context)

    private val taskRepository: TaskRepository = TaskRepositoryImpl(database.taskDao())
    private val settingsRepository: SettingsRepository = SharedPreferencesSettingsRepository(context)

    val observeTasks = ObserveTasksUseCase(taskRepository, settingsRepository)
    val getTask = GetTaskUseCase(taskRepository)
    val saveTask = SaveTaskUseCase(taskRepository)
    val toggleTask = ToggleTaskUseCase(taskRepository)
    val deleteTask = DeleteTaskUseCase(taskRepository)
    val observeSettings = ObserveSettingsUseCase(settingsRepository)
    val updateSettings = UpdateSettingsUseCase(settingsRepository)
}
