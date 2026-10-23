package com.example.taskflow.domain.usecase

import com.example.taskflow.domain.common.AppResult
import com.example.taskflow.domain.common.safeCall
import com.example.taskflow.domain.model.AppSettings
import com.example.taskflow.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveSettingsUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<AppSettings> = repository.settings
}

/** Regra: nome em branco volta ao valor padrão; limite de 30 caracteres. */
class UpdateSettingsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(settings: AppSettings): AppResult<Unit> {
        val name = settings.userName.trim()
        if (name.length > AppSettings.USER_NAME_MAX) {
            return AppResult.Failure(
                "O nome deve ter no máximo ${AppSettings.USER_NAME_MAX} caracteres.",
                isValidation = true
            )
        }
        val normalized = settings.copy(userName = name.ifEmpty { AppSettings.DEFAULT_USER_NAME })
        return safeCall("Não foi possível salvar as configurações.") { repository.update(normalized) }
    }
}
