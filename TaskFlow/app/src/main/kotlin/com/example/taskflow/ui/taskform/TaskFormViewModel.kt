package com.example.taskflow.ui.taskform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.taskflow.di.AppContainer
import com.example.taskflow.domain.common.AppResult
import com.example.taskflow.domain.model.Priority
import com.example.taskflow.domain.model.Task
import com.example.taskflow.domain.usecase.GetTaskUseCase
import com.example.taskflow.domain.usecase.SaveTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Estado de carregamento do formulário (necessário na edição). */
sealed interface FormLoadState {
    data object Loading : FormLoadState
    data class Ready(val task: Task?) : FormLoadState
    data class Error(val message: String) : FormLoadState
}

/** Estado da operação de salvar. */
sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data object Saved : SaveState
    data class Failed(val message: String, val validation: Boolean) : SaveState
}

class TaskFormViewModel(
    private val taskId: Long,
    private val getTask: GetTaskUseCase,
    private val saveTask: SaveTaskUseCase
) : ViewModel() {

    val isNew: Boolean = taskId == Task.NEW_ID

    private var existing: Task? = null

    private val _loadState = MutableStateFlow<FormLoadState>(FormLoadState.Loading)
    val loadState: StateFlow<FormLoadState> = _loadState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (isNew) {
            _loadState.value = FormLoadState.Ready(null)
            return
        }
        _loadState.value = FormLoadState.Loading
        viewModelScope.launch {
            _loadState.value = when (val r = getTask(taskId)) {
                is AppResult.Success -> {
                    existing = r.data
                    FormLoadState.Ready(r.data)
                }
                is AppResult.Failure -> FormLoadState.Error(r.message)
            }
        }
    }

    fun save(title: String, description: String, priority: Priority) {
        if (_saveState.value is SaveState.Saving) return
        _saveState.value = SaveState.Saving
        viewModelScope.launch {
            val base = existing ?: Task(title = "")
            val task = base.copy(
                id = if (isNew) Task.NEW_ID else taskId,
                title = title,
                description = description,
                priority = priority
            )
            _saveState.value = when (val r = saveTask(task)) {
                is AppResult.Success -> SaveState.Saved
                is AppResult.Failure -> SaveState.Failed(r.message, r.isValidation)
            }
        }
    }

    fun clearError() {
        if (_saveState.value is SaveState.Failed) _saveState.value = SaveState.Idle
    }

    companion object {
        fun factory(c: AppContainer, taskId: Long) = viewModelFactory {
            initializer { TaskFormViewModel(taskId, c.getTask, c.saveTask) }
        }
    }
}
