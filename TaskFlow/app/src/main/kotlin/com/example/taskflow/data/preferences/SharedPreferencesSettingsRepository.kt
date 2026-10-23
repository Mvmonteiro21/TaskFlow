package com.example.taskflow.data.preferences

import android.content.Context
import com.example.taskflow.domain.model.AppSettings
import com.example.taskflow.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SharedPreferencesSettingsRepository(context: Context) : SettingsRepository {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val state = MutableStateFlow(read())
    override val settings: Flow<AppSettings> = state.asStateFlow()

    /** Lê valores salvos; se não existirem (ou estiverem corrompidos) usa os padrões. */
    private fun read(): AppSettings {
        val defaults = AppSettings()
        return try {
            AppSettings(
                userName = prefs.getString(KEY_NAME, null) ?: defaults.userName,
                showCompleted = prefs.getBoolean(KEY_SHOW_COMPLETED, defaults.showCompleted),
                sortByPriority = prefs.getBoolean(KEY_SORT, defaults.sortByPriority)
            )
        } catch (e: ClassCastException) {
            defaults
        }
    }

    override suspend fun update(settings: AppSettings) {
        withContext(Dispatchers.IO) {
            val ok = prefs.edit()
                .putString(KEY_NAME, settings.userName)
                .putBoolean(KEY_SHOW_COMPLETED, settings.showCompleted)
                .putBoolean(KEY_SORT, settings.sortByPriority)
                .commit()
            if (!ok) throw java.io.IOException("Falha ao gravar SharedPreferences")
        }
        state.value = settings
    }

    private companion object {
        const val PREFS_NAME = "taskflow_settings"
        const val KEY_NAME = "user_name"
        const val KEY_SHOW_COMPLETED = "show_completed"
        const val KEY_SORT = "sort_by_priority"
    }
}
