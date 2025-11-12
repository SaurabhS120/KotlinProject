package org.example.project.data

object TaskRepoProvider {
    // Default repository used until SQLDelight is wired up
    val repo: TaskRepository = InMemoryTaskRepository()
}
