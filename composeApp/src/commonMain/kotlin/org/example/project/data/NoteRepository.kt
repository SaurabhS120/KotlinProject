package org.example.project.data

import com.example.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

interface NoteRepository {
    val notes: StateFlow<List<Note>>
    suspend fun addNote(title: String, content: String)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Long)
    suspend fun refresh()
}

class SqlDelightNoteRepository(private val database: Database) : NoteRepository {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    override val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val queries = database.notesQueries

    override suspend fun refresh() {
        withContext(Dispatchers.Default) {
            val rows = queries.selectAllNotes().executeAsList()
            _notes.value = rows.map { row ->
                Note(
                    id = row.id,
                    title = row.title,
                    content = row.content ?: "",
                    createdAt = row.created_at
                )
            }
        }
    }

    override suspend fun addNote(title: String, content: String) {
        withContext(Dispatchers.Default) {
            val now = System.currentTimeMillis()
            queries.insertNote(title, content, now)
            refresh()
        }
    }

    override suspend fun updateNote(note: Note) {
        withContext(Dispatchers.Default) {
            queries.updateNote(note.title, note.content, note.id)
            refresh()
        }
    }

    override suspend fun deleteNote(id: Long) {
        withContext(Dispatchers.Default) {
            queries.deleteNote(id)
            refresh()
        }
    }
}
