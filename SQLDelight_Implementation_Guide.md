# SQLDelight Implementation Guide

## Overview

This document explains how SQLDelight was integrated into our Kotlin Multiplatform Compose project
to create a persistent Notes feature. SQLDelight generates typesafe Kotlin APIs from SQL statements,
making database operations both safe and efficient.

## Project Architecture

```
Notes Feature Architecture:
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   NotesScreen   │───▶│ NoteRepository  │───▶│   SQLDelight    │
│      (UI)       │    │   (Business)    │    │   (Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                        ┌─────────────────┐
                        │ DatabaseProvider │
                        │   (Platform)    │
                        └─────────────────┘
```

## Step-by-Step Implementation

### 1. Add SQLDelight Dependencies

#### Version Catalog (`gradle/libs.versions.toml`)

```toml
[versions]
sqldelight = "2.1.0"

[libraries]
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-android-driver = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-sqlite-driver = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }
sqldelight-native-driver = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }

[plugins]
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
```

#### Module Build File (`composeApp/build.gradle.kts`)

```kotlin
plugins {
    // ... other plugins
    alias(libs.plugins.sqldelight)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
        nativeMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
    }
}

// SQLDelight configuration
sqldelight {
    databases {
        create("Database") {
            packageName.set("com.example")
        }
    }
}
```

### 2. Define SQL Schema

Create schema file: `composeApp/src/commonMain/sqldelight/com/example/notes.sq`

```sql
-- Table definition
CREATE TABLE notes (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  content TEXT DEFAULT '',
  created_at INTEGER NOT NULL
);

-- Query definitions
selectAllNotes:
SELECT * FROM notes ORDER BY created_at DESC;

selectNoteById:
SELECT * FROM notes WHERE id = ?;

insertNote:
INSERT INTO notes(title, content, created_at) VALUES (?, ?, ?);

updateNote:
UPDATE notes SET title = ?, content = ? WHERE id = ?;

deleteNote:
DELETE FROM notes WHERE id = ?;
```

**Key Points:**

- File must end with `.sq` extension
- Place in `sqldelight/` directory under your package structure
- Query names become method names in generated code
- Parameters use `?` placeholders

### 3. Create Data Models

#### Note Data Class (`Note.kt`)

```kotlin
package org.example.project.data

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: Long,
    val title: String,
    val content: String = "",
    val createdAt: Long
)
```

### 4. Implement Repository Pattern

#### Repository Interface (`NoteRepository.kt`)

```kotlin
package org.example.project.data

import kotlinx.coroutines.flow.StateFlow

interface NoteRepository {
    val notes: StateFlow<List<Note>>
    suspend fun addNote(title: String, content: String)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Long)
    suspend fun refresh()
}
```

#### SQLDelight Implementation

```kotlin
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
    
    // ... other methods
}
```

**Key Concepts:**

- Use `StateFlow` to expose reactive data to UI
- Execute queries on background thread (`Dispatchers.Default`)
- Map SQL result rows to domain models
- Call `refresh()` after mutations to update StateFlow

### 5. Platform-Specific Database Initialization

#### Android (`DatabaseProvider.android.kt`)

```kotlin
package org.example.project.data

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.Database

object DatabaseProvider {
    private var database: Database? = null

    fun getDatabase(context: Context): Database {
        return database ?: synchronized(this) {
            database ?: run {
                val driver = AndroidSqliteDriver(Database.Schema, context, "notes.db")
                Database(driver).also { database = it }
            }
        }
    }
}
```

#### JVM/Desktop (`DatabaseProvider.jvm.kt`)

```kotlin
package org.example.project.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.Database

object DatabaseProvider {
    private var database: Database? = null

    fun getDatabase(): Database {
        return database ?: synchronized(this) {
            database ?: run {
                val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
                Database.Schema.create(driver)
                Database(driver).also { database = it }
            }
        }
    }
}
```

**Platform Differences:**

- **Android**: Uses `AndroidSqliteDriver`, requires Context, creates persistent file
- **JVM**: Uses `JdbcSqliteDriver`, can be in-memory or file-based
- **iOS**: Would use `NativeSqliteDriver`

### 6. Dependency Injection Setup

#### Repository Provider (`NoteRepoProvider.kt`)

```kotlin
package org.example.project.data

object NoteRepoProvider {
    lateinit var repository: NoteRepository
        private set

    fun initialize(repo: NoteRepository) {
        repository = repo
    }
}
```

#### Platform Initialization (Android)

```kotlin
// In MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize database
        val database = DatabaseProvider.getDatabase(applicationContext)
        NoteRepoProvider.initialize(SqlDelightNoteRepository(database))
        
        setContent { App() }
    }
}
```

### 7. UI Implementation with Compose

#### NotesScreen (`NotesScreen.kt`)

```kotlin
@Composable
fun NotesScreen(
    repository: NoteRepository,
    modifier: Modifier = Modifier
) {
    val notes by repository.notes.collectAsState(emptyList())
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.refresh()
    }

    Scaffold { paddingValues ->
        Column(modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text("Notes", style = MaterialTheme.typography.headlineMedium)

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(notes) { note ->
                    NoteItem(
                        note = note,
                        onEdit = { /* handle edit */ },
                        onDelete = { scope.launch { repository.deleteNote(note.id) } }
                    )
                }
            }

            Button(
                onClick = { /* show add dialog */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Note")
            }
        }
    }
}
```

**UI Best Practices:**

- Use `collectAsState()` to observe StateFlow
- Call `repository.refresh()` in LaunchedEffect
- Use `rememberCoroutineScope()` for suspend function calls
- Handle loading states and errors appropriately

## Generated Code Structure

After building the project, SQLDelight generates:

```
build/generated/sqldelight/code/Database/
├── main/
│   └── com/example/
│       ├── Database.kt              # Main database interface
│       ├── NotesQueries.kt         # Query interface
│       └── Notes.kt                # Table data class
```

### Generated API Example

```kotlin
// Generated Database interface
interface Database {
    val notesQueries: NotesQueries
    companion object {
        val Schema: SqlSchema
    }
}

// Generated NotesQueries interface
interface NotesQueries {
    fun selectAllNotes(): Query<Notes>
    fun insertNote(title: String, content: String?, created_at: Long)
    fun updateNote(title: String, content: String?, id: Long)
    fun deleteNote(id: Long)
}
```

## Build Process

### 1. Gradle Sync

```bash
./gradlew build
```

This generates the SQLDelight code and resolves import errors.

### 2. Database Schema Migration

When changing schema, SQLDelight can generate migration files:

```sql
-- In a new .sqm file
ALTER TABLE notes ADD COLUMN priority INTEGER DEFAULT 0;
```

## Testing Strategy

### Unit Testing Repositories

```kotlin
class NoteRepositoryTest {
    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    private val database = Database(driver)
    private val repository = SqlDelightNoteRepository(database)

    @Before
    fun setup() {
        Database.Schema.create(driver)
    }

    @Test
    fun addNote_updatesStateFlow() = runTest {
        repository.addNote("Test", "Content")
        val notes = repository.notes.first()
        assertEquals(1, notes.size)
        assertEquals("Test", notes[0].title)
    }
}
```

## Debugging Tips

### 1. Enable SQL Logging

```kotlin
// In debug builds
val driver = AndroidSqliteDriver(
    schema = Database.Schema,
    context = context,
    name = "notes.db",
    callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
        override fun onOpen(db: SupportSQLiteDatabase) {
            db.execSQL("PRAGMA foreign_keys=ON;")
        }
    }
)
```

### 2. Database Inspector (Android Studio)

- Go to View → Tool Windows → Database Inspector
- Select your device and app
- Browse tables and data in real-time

### 3. Common Issues

- **Unresolved references**: Run `./gradlew build` to generate code
- **Database locked**: Ensure proper driver cleanup
- **Type mismatches**: Check SQL column types vs Kotlin types

## Performance Considerations

### 1. Query Optimization

```sql
-- Create indexes for frequently queried columns
CREATE INDEX notes_created_at_idx ON notes(created_at);

-- Use LIMIT for large datasets
selectRecentNotes:
SELECT * FROM notes ORDER BY created_at DESC LIMIT ?;
```

### 2. Flow vs Direct Queries

```kotlin
// For reactive UI - use Flow
fun observeNotes(): Flow<List<Note>> = 
    queries.selectAllNotes()
        .asFlow()
        .mapToList(Dispatchers.IO)

// For one-time queries - use direct execution
suspend fun getNoteById(id: Long): Note? = withContext(Dispatchers.IO) {
    queries.selectNoteById(id).executeAsOneOrNull()?.toNote()
}
```

## Migration from Room/Other ORMs

### Advantages of SQLDelight

- **Compile-time safety**: SQL errors caught at build time
- **Multiplatform**: Same code works on all platforms
- **Raw SQL**: Full control over queries
- **Lightweight**: Minimal runtime overhead

### Migration Steps

1. Replace Room annotations with SQLDelight schema files
2. Convert DAO interfaces to repository implementations
3. Update dependency injection setup
4. Test thoroughly on all platforms

## Advanced Features

### 1. Transactions

```kotlin
suspend fun transferNotes(fromId: Long, toId: Long) {
    database.transaction {
        queries.deleteNote(fromId)
        queries.updateNote("Transferred", "content", toId)
    }
}
```

### 2. Custom Types

```sql
-- Define custom column type
import kotlin.collections.List;

CREATE TABLE note_tags (
  note_id INTEGER NOT NULL,
  tags TEXT AS List<String> NOT NULL
);
```

### 3. Views and Complex Queries

```sql
CREATE VIEW note_summary AS
SELECT id, title, length(content) as content_length, created_at
FROM notes
WHERE content IS NOT NULL;

selectNoteSummaries:
SELECT * FROM note_summary ORDER BY content_length DESC;
```

## Conclusion

SQLDelight provides a powerful, type-safe way to work with databases in Kotlin Multiplatform
projects. By generating code from SQL schemas, it ensures compile-time safety while maintaining the
flexibility of raw SQL queries. The reactive APIs integrate seamlessly with Compose UI, making it
ideal for modern Android development.

Key benefits in this project:

- ✅ Type-safe database operations
- ✅ Multiplatform compatibility
- ✅ Reactive UI updates via StateFlow
- ✅ Clean architecture with repository pattern
- ✅ Efficient query generation and execution

The Notes feature demonstrates a complete CRUD implementation using SQLDelight, serving as a
template for other database-backed features in the application.