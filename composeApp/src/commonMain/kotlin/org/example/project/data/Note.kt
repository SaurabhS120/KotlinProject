package org.example.project.data

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: Long,
    val title: String,
    val content: String = "",
    val createdAt: Long
)
