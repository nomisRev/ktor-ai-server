package org.jetbrains

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.chat.ui.ChatScreen

@Composable
@Preview
fun App() = MaterialTheme {
    ChatScreen()
}
