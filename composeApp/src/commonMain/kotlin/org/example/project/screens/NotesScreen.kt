package org.example.project.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.data.Note
import org.example.project.data.NoteRepository

@Composable
fun NotesScreen(
    repository: NoteRepository,
    modifier: Modifier = Modifier
) {
    val notes by repository.notes.collectAsState(emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.refresh()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text("Notes", style = MaterialTheme.typography.headlineMedium)

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(notes) { note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(note.title, style = MaterialTheme.typography.titleMedium)
                            if (note.content.isNotBlank()) {
                                Text(note.content, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Row {
                            TextButton(onClick = {
                                editingNote = note
                                showDialog = true
                            }) { Text("Edit") }

                            TextButton(onClick = {
                                scope.launch { repository.deleteNote(note.id) }
                            }) { Text("Delete") }
                        }
                    }
                }
            }

            Button(onClick = {
                editingNote = null
                showDialog = true
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Add Note")
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf(editingNote?.title ?: "") }
        var content by remember { mutableStateOf(editingNote?.content ?: "") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    if (editingNote == null) {
                        scope.launch { repository.addNote(title, content) }
                    } else {
                        scope.launch {
                            repository.updateNote(
                                editingNote!!.copy(
                                    title = title,
                                    content = content
                                )
                            )
                        }
                    }
                    showDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
            title = { Text(if (editingNote == null) "Add Note" else "Edit Note") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }
            }
        )
    }
}
