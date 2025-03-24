package io.ktor.samples.chat.frontend

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.browser.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.dom.createElement
import org.w3c.dom.*
import org.w3c.dom.events.*

@OptIn(DelicateCoroutinesApi::class)
val scope: CoroutineScope = GlobalScope

fun main() {
    WsClient(HttpClient { install(WebSockets) }).use { client ->
        scope.launch { initConnection(client) }

        document.addEventListener("DOMContentLoaded", {
            val sendButton = document.getElementById("sendButton") as HTMLElement
            val commandInput = document.getElementById("commandInput") as HTMLInputElement

            sendButton.addEventListener("click", {
                scope.launch { sendMessage(client, commandInput) }
            })
            commandInput.addEventListener("keydown", { e ->
                if ((e as KeyboardEvent).key == "Enter") {
                    scope.launch { sendMessage(client, commandInput) }
                }
            })
        })
    }
}


suspend fun initConnection(wsClient: WsClient) {
    try {
        wsClient.connect()
        wsClient.receive(::writeMessage)
    } catch (e: Exception) {
        if (e is ClosedReceiveChannelException) {
            writeMessage("Disconnected. ${e.message}.")
        } else if (e is WebSocketException) {
            writeMessage("Unable to connect.")
        }

        window.setTimeout({
            scope.launch { initConnection(wsClient) }
        }, 5000)
    }
}

suspend fun sendMessage(client: WsClient, input: HTMLInputElement) {
    if (input.value.isNotEmpty()) {
        client.send(input.value)
        input.value = ""
    }
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

class WsClient(private val client: HttpClient) : AutoCloseable by client {
    var session: WebSocketSession? = null

    suspend fun connect() {
        session = client.webSocketSession(
            method = HttpMethod.Get,
            host = window.location.hostname,
            port = window.location.port.toInt(),
            path = "/ws"
        )
    }

    suspend fun send(message: String) {
        session?.send(Frame.Text(message))
    }

    suspend fun receive(onReceive: (input: String) -> Unit) {
        while (true) {
            val frame = session?.incoming?.receive()

            if (frame is Frame.Text) {
                onReceive(frame.readText())
            }
        }
    }
}