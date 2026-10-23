package com.example.taskflow.domain.model

/** Configurações simples do app (persistidas em SharedPreferences). */
data class AppSettings(
    val userName: String = DEFAULT_USER_NAME,
    val showCompleted: Boolean = true,
    val sortByPriority: Boolean = false
) {
    companion object {
        const val DEFAULT_USER_NAME = "Estudante"
        const val USER_NAME_MAX = 30
    }
}
