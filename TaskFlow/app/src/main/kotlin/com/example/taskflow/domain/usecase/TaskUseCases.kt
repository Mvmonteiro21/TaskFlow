package com.example.taskflow.domain.usecase

import com.example.taskflow.domain.common.AppResult
import com.example.taskflow.domain.common.safeCall
import com.example.taskflow.domain.model.Task
import com.example.taskflow.domain.model.TaskOverview
import com.example.taskflow.domain.repository.SettingsRepository
import com.example.taskflow.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Regra: aplica filtro "mostrar concluídas" e ordenação por prioridade das configurações. */
class ObserveTasksUseCase(
    private val tasks: TaskRepository,
    private val settings: SettingsRepository
) {
    operator fun invoke(): Flow<TaskOverview> =
        combine(tasks.observeTasks(), settings.settings) { all, s ->
            var visible = if (s.showCompleted) all else all.filter { !it.done }
            if (s.sortByPriority) {
                visible = visible.sortedWith(
                    compareBy<Task> { it.done }.thenByDescending { it.priority.ordinal }
                )
            }
            TaskOverview(
                tasks = visible,
                userName = s.userName,
                pendingCount = all.count { !it.done },
                totalCount = all.size
            )
        }
}

class GetTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(id: Long): AppResult<Task> {
        return when (val result = safeCall("Não foi possível carregar a tarefa.") { repository.getById(id) }) {
            is AppResult.Failure -> result
            is AppResult.Success ->
                result.data?.let { AppResult.Success(it) }
                    ?: AppResult.Failure("Tarefa não encontrada.")
        }
    }
}

/** Regra: valida título (3..60) e descrição (<=200) antes de gravar (cadastro e atualização). */
class SaveTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): AppResult<Long> {
        val title = task.title.trim()
        val description = task.description.trim()
        val validation = when {
            title.isEmpty() -> "O título é obrigatório."
            title.length < Task.TITLE_MIN -> "O título deve ter pelo menos ${Task.TITLE_MIN} caracteres."
            title.length > Task.TITLE_MAX -> "O título deve ter no máximo ${Task.TITLE_MAX} caracteres."
            description.length > Task.DESCRIPTION_MAX -> "A descrição deve ter no máximo ${Task.DESCRIPTION_MAX} caracteres."
            else -> null
        }
        if (validation != null) return AppResult.Failure(validation, isValidation = true)

        return safeCall("Não foi possível salvar a tarefa. Tente novamente.") {
            repository.save(task.copy(title = title, description = description))
        }
    }
}

class ToggleTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(id: Long, done: Boolean): AppResult<Unit> =
        safeCall("Não foi possível atualizar a tarefa.") { repository.setDone(id, done) }
}

class DeleteTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(id: Long): AppResult<Unit> =
        safeCall("Não foi possível excluir a tarefa.") { repository.delete(id) }
}
