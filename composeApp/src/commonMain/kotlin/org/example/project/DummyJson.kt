package org.example.project

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
class DummyJson {
    private val client = HttpClient()

    suspend fun get(): String {
        val response = client.get("https://dummyjson.com/test")
        return response.body()
    }
}