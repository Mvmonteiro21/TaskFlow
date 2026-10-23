package com.example.taskflow.data.repository

import com.example.taskflow.data.local.TaskDao
import com.example.taskflow.data.local.toDomain
import com.example.taskflow.data.local.toEntity
import com.example.taskflow.domain.model.Task
import com.example.taskflow.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(private val dao: TaskDao) : TaskRepository {
    override fun observeTasks(): Flow<List<Task>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Task? = dao.getById(id)?.toDomain()

    override suspend fun save(task: Task): Long = dao.upsert(task.toEntity())

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun setDone(id: Long, done: Boolean) = dao.setDone(id, done)
}
