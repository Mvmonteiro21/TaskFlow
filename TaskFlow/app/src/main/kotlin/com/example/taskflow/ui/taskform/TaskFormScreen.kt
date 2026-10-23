package com.example.taskflow.ui.taskform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskflow.domain.model.Priority
import com.example.taskflow.domain.model.Task
import com.example.taskflow.ui.components.ErrorState
import com.example.taskflow.ui.components.LoadingState
import com.example.taskflow.ui.components.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(viewModel: TaskFormViewModel, onBack: () -> Unit) {
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()

    // Volta para a lista assim que o salvamento termina com sucesso
    LaunchedEffect(saveState) {
        if (saveState is SaveState.Saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isNew) "Nova tarefa" else "Editar tarefa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = loadState) {
                FormLoadState.Loading -> LoadingState(Modifier.align(Alignment.Center))
                is FormLoadState.Error -> ErrorState(
                    message = s.message,
                    onRetry = viewModel::load,
                    modifier = Modifier.align(Alignment.Center)
                )
                is FormLoadState.Ready -> TaskFormContent(
                    initial = s.task,
                    saveState = saveState,
                    onSave = viewModel::save,
                    onInputChanged = viewModel::clearError
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskFormContent(
    initial: Task?,
    saveState: SaveState,
    onSave: (String, String, Priority) -> Unit,
    onInputChanged: () -> Unit
) {
    // rememberSaveable: o que o usuário digitou sobrevive a rotação / recriação do processo
    var title by rememberSaveable { mutableStateOf(initial?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initial?.description.orEmpty()) }
    var priority by rememberSaveable { mutableStateOf(initial?.priority ?: Priority.MEDIUM) }

    val failed = saveState as? SaveState.Failed
    val validationError = failed?.takeIf { it.validation }?.message
    val generalError = failed?.takeIf { !it.validation }?.message
    val saving = saveState is SaveState.Saving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it; onInputChanged() },
            label = { Text("Título *") },
            singleLine = true,
            isError = validationError != null,
            supportingText = { Text(validationError ?: "${title.length}/${Task.TITLE_MAX}") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it; onInputChanged() },
            label = { Text("Descrição") },
            minLines = 3,
            supportingText = { Text("${description.length}/${Task.DESCRIPTION_MAX}") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Prioridade", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Priority.entries.forEach { p ->
                FilterChip(
                    selected = priority == p,
                    onClick = { priority = p },
                    label = { Text(p.label()) }
                )
            }
        }

        generalError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { onSave(title, description, priority) },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (saving) "Salvando..." else "Salvar")
        }
    }
}
