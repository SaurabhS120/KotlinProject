package org.example.project.data

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Long,
    val title: String,
    val description: String = "",
    val completed: Boolean = false
)
