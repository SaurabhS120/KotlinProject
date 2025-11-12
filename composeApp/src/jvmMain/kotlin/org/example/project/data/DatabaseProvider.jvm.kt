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
