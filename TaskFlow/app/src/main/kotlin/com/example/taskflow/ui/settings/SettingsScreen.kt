package com.example.taskflow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskflow.domain.model.AppSettings
import com.example.taskflow.ui.components.ErrorState
import com.example.taskflow.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                SettingsUiState.Loading -> LoadingState(Modifier.align(Alignment.Center))
                is SettingsUiState.Error -> ErrorState(
                    message = s.message,
                    onRetry = onBack,
                    modifier = Modifier.align(Alignment.Center)
                )
                is SettingsUiState.Success -> SettingsContent(s.settings, viewModel::save)
            }
        }
    }
}

@Composable
private fun SettingsContent(saved: AppSettings, onSave: (AppSettings) -> Unit) {
    var name by rememberSaveable { mutableStateOf(saved.userName) }
    var showCompleted by rememberSaveable { mutableStateOf(saved.showCompleted) }
    var sortByPriority by rememberSaveable { mutableStateOf(saved.sortByPriority) }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Seu nome") },
            singleLine = true,
            supportingText = { Text("Em branco = \"${AppSettings.DEFAULT_USER_NAME}\"") },
            modifier = Modifier.fillMaxWidth()
        )
        SwitchRow("Mostrar tarefas concluídas", showCompleted) { showCompleted = it }
        SwitchRow("Ordenar por prioridade", sortByPriority) { sortByPriority = it }
        Button(
            onClick = { onSave(AppSettings(name, showCompleted, sortByPriority)) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Salvar configurações") }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
