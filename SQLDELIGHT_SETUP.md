# SQLDelight Setup for Notes Feature

## What Was Done

### 1. Added SQLDelight to Version Catalog (`gradle/libs.versions.toml`)

- Added `sqldelight = "2.1.0"` version
- Added runtime and driver libraries:
    - `sqldelight-runtime`
    - `sqldelight-android-driver`
    - `sqldelight-sqlite-driver` (for JVM/Desktop)
    - `sqldelight-native-driver` (for iOS)
- Added `sqldelight` plugin

### 2. Updated Build Configuration (`composeApp/build.gradle.kts`)

- Added SQLDelight plugin: `alias(libs.plugins.sqldelight)`
- Added runtime dependency to `commonMain`
- Added platform-specific drivers:
    - Android: `android-driver`
    - JVM: `sqlite-driver`
    - Native (iOS): `native-driver`
- Added SQLDelight database configuration:
  ```kotlin
  sqldelight {
      databases {
          create("Database") {
              packageName.set("com.example")
          }
      }
  }
  ```

### 3. Created SQL Schema (`composeApp/src/commonMain/sqldelight/com/example/notes.sq`)

- Table: `notes` with columns:
    - `id` (INTEGER, PRIMARY KEY, AUTOINCREMENT)
    - `title` (TEXT, NOT NULL)
    - `content` (TEXT, DEFAULT '')
    - `created_at` (INTEGER, NOT NULL)
- Queries:
    - `selectAllNotes`: Get all notes ordered by creation date
    - `selectNoteById`: Get a specific note
    - `insertNote`: Add new note
    - `updateNote`: Update existing note
    - `deleteNote`: Delete note

### 4. Created Data Models

- **`Note.kt`**: Data class for notes
  ```kotlin
  data class Note(
      val id: Long,
      val title: String,
      val content: String = "",
      val createdAt: Long
  )
  ```

- **`NoteRepository.kt`**: Repository interface and SQLDelight implementation
    - `SqlDelightNoteRepository` uses generated `Database` class
    - Methods: `addNote()`, `updateNote()`, `deleteNote()`, `refresh()`
    - Exposes `StateFlow<List<Note>>` for observing notes

- **`NoteRepoProvider.kt`**: Singleton to hold repository instance

### 5. Created Platform-Specific Database Providers

- **Android** (`DatabaseProvider.android.kt`):
    - Uses `AndroidSqliteDriver`
    - Creates `notes.db` database

- **JVM/Desktop** (`DatabaseProvider.jvm.kt`):
    - Uses `JdbcSqliteDriver`
    - Creates in-memory database for now

### 6. Created NotesScreen UI (`NotesScreen.kt`)

- List view showing all notes
- Add/Edit/Delete functionality via dialogs
- Uses `NoteRepository` interface
- Automatically refreshes on load

### 7. Updated Navigation

- Added `Notes` route to `Screen.kt`
- Added `NotesScreen` composable to `NavGraph.kt`
- Added "Go to Notes (SQLDelight)" button in `HomeScreen.kt`

### 8. Initialized Database in Android

- Updated `MainActivity.kt` to:
    - Get database instance from `DatabaseProvider`
    - Create `SqlDelightNoteRepository`
    - Initialize `NoteRepoProvider`

## Next Steps

### 1. Sync Gradle & Build Project

**IMPORTANT**: Run a Gradle sync to generate SQLDelight code:

```bash
./gradlew build
```

This will generate:

- `com.example.Database` class
- `com.example.NotesQueries` interface
- Query result data classes

### 2. Fix Linter Errors

After the build, all unresolved references to `Database`, `notesQueries`, etc. will be resolved.

### 3. Test on Android

- Run the app on Android
- Navigate to "Go to Notes (SQLDelight)"
- Test adding, editing, and deleting notes
- Notes will persist in SQLite database

### 4. (Optional) Add Desktop/JVM Support

If you want to run on desktop, you need to:

1. Add JVM target to `build.gradle.kts`:
   ```kotlin
   jvm("desktop")
   ```
2. Create desktop main function that initializes the database
3. Configure desktop application packaging

### 5. (Optional) Add iOS Support

For iOS, create platform-specific initialization in iOS app delegate or SwiftUI app:

```kotlin
// In iosMain
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.Database

fun createDatabase(): Database {
    val driver = NativeSqliteDriver(Database.Schema, "notes.db")
    return Database(driver)
}
```

## File Structure

```
composeApp/
├── build.gradle.kts (SQLDelight plugin & dependencies)
├── src/
│   ├── commonMain/
│   │   ├── kotlin/org/example/project/
│   │   │   ├── data/
│   │   │   │   ├── Note.kt
│   │   │   │   ├── NoteRepository.kt
│   │   │   │   └── NoteRepoProvider.kt
│   ���   │   └── screens/
│   │   │       └── NotesScreen.kt
│   │   └── sqldelight/com/example/
│   │       └── notes.sq
│   ├── androidMain/kotlin/org/example/project/
│   │   ├── MainActivity.kt (initialization)
│   │   └── data/
│   │       └── DatabaseProvider.android.kt
│   └── jvmMain/kotlin/org/example/project/
│       └── data/
│           └── DatabaseProvider.jvm.kt
```

## Usage

Once Gradle sync completes:

1. **Run on Android**:
   ```bash
   ./gradlew installDebug
   ```

2. **Navigate to Notes**:
    - Open app → Home Screen → "Go to Notes (SQLDelight)"

3. **Add Notes**:
    - Tap "Add Note" → Enter title and content → Save
    - Notes are saved to SQLite database

4. **Edit Notes**:
    - Tap "Edit" on any note → Modify → Save

5. **Delete Notes**:
    - Tap "Delete" on any note

## Notes

- The existing "Tasks" feature still uses in-memory storage
- Notes use SQLDelight for persistent storage
- Both features coexist to demonstrate different approaches
- Android database file location: `/data/data/org.example.project/databases/notes.db`
