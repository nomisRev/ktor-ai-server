package org.jetbrains

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.chat.ui.ChatScreen

fun isLoggedIn(): Boolean {
    val currentUrl = window.location.href
    return when {
        currentUrl.contains("home") -> true
        currentUrl.contains("login") -> false
        else -> false
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() =
    ComposeViewport(document.body!!) {
        MaterialTheme {
            if (isLoggedIn()) ChatScreen() else window.location.href = "http://localhost/login"
        }
    }
