package io.ktor.samples.chat.frontend

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.*
import io.ktor.websocket.*
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.browser.*
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.dom.createElement
import org.w3c.dom.*
import org.w3c.dom.events.*

@OptIn(DelicateCoroutinesApi::class)
private val scope: CoroutineScope = GlobalScope

fun main() {
    val wsClient = WsClient(HttpClient { install(WebSockets) })
    scope.launch { wsClient.initConnection() }

    document.addEventListener("DOMContentLoaded", {
        val sendButton = document.getElementById("sendButton") as HTMLElement
        val commandInput = document.getElementById("commandInput") as HTMLInputElement

        sendButton.addEventListener("click", {
            scope.launch { wsClient.sendMessage(commandInput) }
        })
        commandInput.addEventListener("keydown", { e ->
            if ((e as KeyboardEvent).key == "Enter") {
                scope.launch { wsClient.sendMessage(commandInput) }
            }
        })
    })
}

fun writeMessage(message: String) {
    val line = document.createElement("p") {
        className = "message"
        textContent = message
    } as HTMLElement

    val messagesBlock = document.getElementById("messages") as HTMLElement
    messagesBlock.appendChild(line)
    messagesBlock.scrollTop = line.offsetTop.toDouble()
}

class WsClient(private val client: HttpClient) {
    var session: WebSocketSession? = null

    suspend fun initConnection() {
        try {
            val currentSession = client.webSocketSession(
                method = HttpMethod.Get,
                host = window.location.hostname,
                port = window.location.port.toInt(),
                path = "/ws"
            )
            session = currentSession
            currentSession.incoming.consumeAsFlow()
                .filterIsInstance<Frame.Text>()
                .collect { frame -> writeMessage(frame.readText()) }
        } catch (e: Exception) {
            if (e is ClosedReceiveChannelException) {
                writeMessage("Disconnected: ${e.message}. Reconnecting in 5 seconds... ")
            } else if (e is WebSocketException) {
                writeMessage("Unable to connect: ${e.message}.  Reconnecting in 5 seconds...")
            } else {
                writeMessage("Unknown failure: ${e.message}.  Reconnecting in 5 seconds... ")
            }

            window.setTimeout({
                scope.launch { initConnection() }
            }, 5000)
        }
    }

    suspend fun sendMessage(input: HTMLInputElement) {
        if (input.value.isNotEmpty()) {
            session?.send(Frame.Text(input.value))
            input.value = ""
        }
    }
}
