package com.example.taskflow.domain.model

enum class Priority { LOW, MEDIUM, HIGH }

data class Task(
    val id: Long = NEW_ID,
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /** Id 0 indica tarefa ainda não persistida (Room gera o id real). */
        const val NEW_ID = 0L
        const val TITLE_MIN = 3
        const val TITLE_MAX = 60
        const val DESCRIPTION_MAX = 200
    }
}
