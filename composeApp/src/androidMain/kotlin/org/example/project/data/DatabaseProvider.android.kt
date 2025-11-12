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
