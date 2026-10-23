package com.example.taskflow.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.taskflow.di.AppContainer
import com.example.taskflow.domain.common.AppResult
import com.example.taskflow.domain.model.AppSettings
import com.example.taskflow.domain.usecase.ObserveSettingsUseCase
import com.example.taskflow.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val settings: AppSettings) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
}

class SettingsViewModel(
    observeSettings: ObserveSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = observeSettings()
        .map<AppSettings, SettingsUiState> { SettingsUiState.Success(it) }
        .onStart { emit(SettingsUiState.Loading) }
        .catch { emit(SettingsUiState.Error("Não foi possível carregar as configurações.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading)

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun save(settings: AppSettings) {
        viewModelScope.launch {
            when (val r = updateSettings(settings)) {
                is AppResult.Success -> _messages.send("Configurações salvas.")
                is AppResult.Failure -> _messages.send(r.message)
            }
        }
    }

    companion object {
        fun factory(c: AppContainer) = viewModelFactory {
            initializer { SettingsViewModel(c.observeSettings, c.updateSettings) }
        }
    }
}
