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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.data.Task
import org.example.project.data.TaskRepository

@Composable
fun TasksScreen(
    repository: TaskRepository,
    modifier: Modifier = Modifier
) {
    val tasks by repository.tasks.collectAsState(emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text("Tasks", style = MaterialTheme.typography.headlineMedium)

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(tasks) { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1F)) {
                            Text(task.title, style = MaterialTheme.typography.titleMedium)
                            if (task.description.isNotBlank()) {
                                Text(task.description, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Row {
                            TextButton(onClick = {
                                editingTask = task
                                showDialog = true
                            }) { Text("Edit") }

                            TextButton(onClick = {
                                scope.launch { repository.deleteTask(task.id) }
                            }) { Text("Delete") }
                        }
                    }
                }
            }

            Button(onClick = {
                editingTask = null
                showDialog = true
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Add Task")
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf(editingTask?.title ?: "") }
        var description by remember { mutableStateOf(editingTask?.description ?: "") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    if (editingTask == null) {
                        scope.launch { repository.addTask(title, description) }
                    } else {
                        scope.launch {
                            repository.updateTask(
                                editingTask!!.copy(
                                    title = title,
                                    description = description
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
            title = { Text(if (editingTask == null) "Add Task" else "Edit Task") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }
            }
        )
    }
}
