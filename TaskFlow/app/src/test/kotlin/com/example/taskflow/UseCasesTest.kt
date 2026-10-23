package com.example.taskflow

import com.example.taskflow.domain.common.AppResult
import com.example.taskflow.domain.model.AppSettings
import com.example.taskflow.domain.model.Priority
import com.example.taskflow.domain.model.Task
import com.example.taskflow.domain.repository.SettingsRepository
import com.example.taskflow.domain.repository.TaskRepository
import com.example.taskflow.domain.usecase.GetTaskUseCase
import com.example.taskflow.domain.usecase.ObserveTasksUseCase
import com.example.taskflow.domain.usecase.SaveTaskUseCase
import com.example.taskflow.domain.usecase.ToggleTaskUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeTaskRepository : TaskRepository {
    val items = MutableStateFlow<List<Task>>(emptyList())
    var failOnSave = false
    private var nextId = 1L

    override fun observeTasks(): Flow<List<Task>> = items
    override suspend fun getById(id: Long): Task? = items.value.firstOrNull { it.id == id }
    override suspend fun save(task: Task): Long {
        if (failOnSave) throw IllegalStateException("disk error")
        val id = if (task.id == Task.NEW_ID) nextId++ else task.id
        items.value = items.value.filter { it.id != id } + task.copy(id = id)
        return id
    }
    override suspend fun delete(id: Long) { items.value = items.value.filter { it.id != id } }
    override suspend fun setDone(id: Long, done: Boolean) {
        items.value = items.value.map { if (it.id == id) it.copy(done = done) else it }
    }
}

private class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    val state = MutableStateFlow(initial)
    override val settings: Flow<AppSettings> = state
    override suspend fun update(settings: AppSettings) { state.value = settings }
}

class UseCasesTest {

    @Test
    fun `titulo vazio retorna erro de validacao`() = runBlocking {
        val result = SaveTaskUseCase(FakeTaskRepository())(Task(title = "   "))
        assertTrue(result is AppResult.Failure && result.isValidation)
    }

    @Test
    fun `titulo curto retorna erro de validacao`() = runBlocking {
        val result = SaveTaskUseCase(FakeTaskRepository())(Task(title = "ab"))
        assertTrue(result is AppResult.Failure && result.isValidation)
    }

    @Test
    fun `cadastro valido grava com titulo sem espacos`() = runBlocking {
        val repo = FakeTaskRepository()
        val result = SaveTaskUseCase(repo)(Task(title = "  Estudar Room  "))
        assertTrue(result is AppResult.Success)
        assertEquals("Estudar Room", repo.items.value.single().title)
    }

    @Test
    fun `atualizacao mantem o mesmo id`() = runBlocking {
        val repo = FakeTaskRepository()
        val save = SaveTaskUseCase(repo)
        val id = (save(Task(title = "Original")) as AppResult.Success).data
        save(Task(id = id, title = "Editada", priority = Priority.HIGH))
        assertEquals(1, repo.items.value.size)
        assertEquals("Editada", repo.items.value.single().title)
    }

    @Test
    fun `falha no repositorio vira Failure sem excecao`() = runBlocking {
        val repo = FakeTaskRepository().apply { failOnSave = true }
        val result = SaveTaskUseCase(repo)(Task(title = "Tarefa valida"))
        assertTrue(result is AppResult.Failure && !result.isValidation)
    }

    @Test
    fun `tarefa inexistente retorna Failure`() = runBlocking {
        assertTrue(GetTaskUseCase(FakeTaskRepository())(99) is AppResult.Failure)
    }

    @Test
    fun `ocultar concluidas filtra a lista e mantem contadores`() = runBlocking {
        val repo = FakeTaskRepository()
        val settings = FakeSettingsRepository(AppSettings(showCompleted = false))
        val save = SaveTaskUseCase(repo)
        val id = (save(Task(title = "Primeira")) as AppResult.Success).data
        save(Task(title = "Segunda"))
        ToggleTaskUseCase(repo)(id, true)

        val overview = ObserveTasksUseCase(repo, settings)().first()
        assertEquals(1, overview.tasks.size)
        assertEquals(2, overview.totalCount)
        assertEquals(1, overview.pendingCount)
        assertFalse(overview.tasks.any { it.done })
    }

    @Test
    fun `ordenar por prioridade coloca alta primeiro`() = runBlocking {
        val repo = FakeTaskRepository()
        val settings = FakeSettingsRepository(AppSettings(sortByPriority = true))
        val save = SaveTaskUseCase(repo)
        save(Task(title = "Baixa", priority = Priority.LOW))
        save(Task(title = "Alta", priority = Priority.HIGH))
        val overview = ObserveTasksUseCase(repo, settings)().first()
        assertEquals("Alta", overview.tasks.first().title)
    }
}
