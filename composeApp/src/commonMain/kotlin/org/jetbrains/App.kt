package org.jetbrains

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.chat.ui.ChatScreen

@Composable
@Preview
fun App() = MaterialTheme {
    ChatScreen()
}
