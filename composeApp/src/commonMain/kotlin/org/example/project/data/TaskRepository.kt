package org.example.project.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Simple in-memory repository for tasks. Replace with SQLDelight implementation later.
 */
interface TaskRepository {
    val tasks: StateFlow<List<Task>>

    suspend fun addTask(title: String, description: String)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: Long)
}

class InMemoryTaskRepository : TaskRepository {
    private val mutex = Mutex()
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    override val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private var nextId = 1L

    override suspend fun addTask(title: String, description: String) {
        mutex.withLock {
            val task = Task(id = nextId++, title = title, description = description)
            _tasks.value = _tasks.value + task
        }
    }

    override suspend fun updateTask(updated: Task) {
        mutex.withLock {
            _tasks.value = _tasks.value.map { if (it.id == updated.id) updated else it }
        }
    }

    override suspend fun deleteTask(id: Long) {
        mutex.withLock {
            _tasks.value = _tasks.value.filterNot { it.id == id }
        }
    }

    suspend fun clear() {
        mutex.withLock { _tasks.value = emptyList(); nextId = 1L }
    }
}
