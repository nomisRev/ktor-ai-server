package org.jetbrains

import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*

@OptIn(ExperimentalComposeUiApi::class)
fun main() = ComposeViewport(document.body!!) {
    val coroutineScope = rememberCoroutineScope({ Dispatchers.Default })
    MaterialTheme {
        HttpClient().use { client ->
            Button(onClick = {
                coroutineScope.launch {
                    try {
                        val response = client.get("localhost:8000/login")
                        println("Initial response status: ${response.status}")
                    } catch (e: Exception) {
                        println("Error: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }) {
                Text("Login")
            }
//        ChatScreen()
        }
    }
}
