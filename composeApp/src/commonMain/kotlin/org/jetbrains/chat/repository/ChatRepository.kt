package org.jetbrains.chat.repository

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface ChatRepository {
    fun connect(): Flow<String>
    fun sendMessage(message: String)
}

class WebSocketChatRepository(
    private val client: HttpClient,
    private val baseUrl: String,
    private val scope: CoroutineScope
) : ChatRepository {
    private var session: DefaultClientWebSocketSession? = null

    override fun connect(): Flow<String> = flow {
        val s = client.webSocketSession(
            method = HttpMethod.Get,
            host = baseUrl,
            path = "/ws"
        )
        session = s
        s.incoming.consumeAsFlow()
            .filterIsInstance<Frame.Text>()
            .map { it.readText() }
            .collect(this)
    }

    override fun sendMessage(message: String) {
        scope.launch {
            requireNotNull(session?.send(Frame.Text(message))) { "Session is not connected" }
        }
    }
}
