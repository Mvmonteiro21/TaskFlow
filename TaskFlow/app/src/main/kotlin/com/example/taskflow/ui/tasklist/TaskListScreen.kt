package com.example.taskflow.ui.tasklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskflow.domain.model.Task
import com.example.taskflow.domain.model.TaskOverview
import com.example.taskflow.ui.components.ErrorState
import com.example.taskflow.ui.components.LoadingState
import com.example.taskflow.ui.components.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onAddTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    // rememberSaveable: o diálogo sobrevive a rotação de tela
    var taskToDelete by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TaskFlow") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = "Nova tarefa")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                TaskListUiState.Loading -> LoadingState(Modifier.align(Alignment.Center))
                is TaskListUiState.Error -> ErrorState(
                    message = s.message,
                    onRetry = viewModel::retry,
                    modifier = Modifier.align(Alignment.Center)
                )
                is TaskListUiState.Success -> TaskListContent(
                    overview = s.overview,
                    onToggle = viewModel::onToggle,
                    onEdit = onEditTask,
                    onDelete = { taskToDelete = it }
                )
            }
        }
    }

    taskToDelete?.let { id ->
        val title = (state as? TaskListUiState.Success)
            ?.overview?.tasks?.firstOrNull { it.id == id }?.title ?: "esta tarefa"
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Excluir tarefa") },
            text = { Text("Deseja excluir \"$title\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDelete(id)
                    taskToDelete = null
                }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { taskToDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TaskListContent(
    overview: TaskOverview,
    onToggle: (Long, Boolean) -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Olá, ${overview.userName}!", style = MaterialTheme.typography.headlineSmall)
            Text(
                "${overview.pendingCount} pendente(s) de ${overview.totalCount} tarefa(s)",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (overview.tasks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (overview.totalCount == 0) "Nenhuma tarefa cadastrada.\nToque em + para criar a primeira."
                    else "Nenhuma tarefa para exibir com o filtro atual.",
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(overview.tasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        onToggle = { onToggle(task.id, it) },
                        onEdit = { onEdit(task.id) },
                        onDelete = { onDelete(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.done, onCheckedChange = onToggle)
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null
                )
                if (task.description.isNotBlank()) {
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    "Prioridade: ${task.priority.label()}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
        }
    }
}
