package com.example.taskflow.domain.model

/** Resultado da regra de negócio "listar tarefas" (já filtrado/ordenado). */
data class TaskOverview(
    val tasks: List<Task>,
    val userName: String,
    val pendingCount: Int,
    val totalCount: Int
)
