package com.example.taskflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taskflow.domain.model.Priority
import com.example.taskflow.domain.model.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val priority: String,
    val done: Boolean,
    val createdAt: Long
)

fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    priority = runCatching { Priority.valueOf(priority) }.getOrDefault(Priority.MEDIUM),
    done = done,
    createdAt = createdAt
)

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    priority = priority.name,
    done = done,
    createdAt = createdAt
)
