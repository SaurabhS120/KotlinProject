package org.example.project.data

object NoteRepoProvider {
    lateinit var repository: NoteRepository
        private set

    fun initialize(repo: NoteRepository) {
        repository = repo
    }
}
