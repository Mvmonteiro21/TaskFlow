package com.example.taskflow.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.taskflow.di.AppContainer
import com.example.taskflow.domain.common.AppResult
import com.example.taskflow.domain.model.TaskOverview
import com.example.taskflow.domain.usecase.DeleteTaskUseCase
import com.example.taskflow.domain.usecase.ObserveTasksUseCase
import com.example.taskflow.domain.usecase.ToggleTaskUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface TaskListUiState {
    data object Loading : TaskListUiState
    data class Success(val overview: TaskOverview) : TaskListUiState
    data class Error(val message: String) : TaskListUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModel(
    observeTasks: ObserveTasksUseCase,
    private val toggleTask: ToggleTaskUseCase,
    private val deleteTask: DeleteTaskUseCase
) : ViewModel() {

    private val reloadTrigger = MutableStateFlow(0)

    val uiState: StateFlow<TaskListUiState> = reloadTrigger
        .flatMapLatest {
            observeTasks()
                .map<TaskOverview, TaskListUiState> { TaskListUiState.Success(it) }
                .onStart { emit(TaskListUiState.Loading) }
                .catch { emit(TaskListUiState.Error("Não foi possível carregar suas tarefas.")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskListUiState.Loading)

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun retry() {
        reloadTrigger.value += 1
    }

    fun onToggle(id: Long, done: Boolean) {
        viewModelScope.launch {
            val result = toggleTask(id, done)
            if (result is AppResult.Failure) _messages.send(result.message)
        }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch {
            when (val result = deleteTask(id)) {
                is AppResult.Success -> _messages.send("Tarefa excluída.")
                is AppResult.Failure -> _messages.send(result.message)
            }
        }
    }

    companion object {
        fun factory(c: AppContainer) = viewModelFactory {
            initializer { TaskListViewModel(c.observeTasks, c.toggleTask, c.deleteTask) }
        }
    }
}
