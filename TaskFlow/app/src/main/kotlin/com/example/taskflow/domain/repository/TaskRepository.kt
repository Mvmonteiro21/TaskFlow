package com.example.taskflow.domain.repository

import com.example.taskflow.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    suspend fun getById(id: Long): Task?
    suspend fun save(task: Task): Long
    suspend fun delete(id: Long)
    suspend fun setDone(id: Long, done: Boolean)
}
