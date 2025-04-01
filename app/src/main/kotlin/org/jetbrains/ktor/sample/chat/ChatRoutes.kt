package org.jetbrains.ktor.sample.chat

import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.Routing
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.serialization.Serializable
import org.jetbrains.ktor.sample.ai.AiService

@Serializable
data class UserSession(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Long
)

fun Routing.installChatRoutes(ai: Deferred<AiService>) {
    staticResources("", "web")

    webSocket("/ws") {
        val session = call.sessions.get<UserSession>()
        if (session == null) return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
        send(Frame.Text("Hey, I am your personal travel assistant. How may I help you today?"))
        send(Frame.Text("### END ###"))
        incoming.consumeAsFlow()
            .filterIsInstance<Frame.Text>()
            .collect { frame ->
                val question = frame.readText()
                // TODO: Replace with memoryId / userId from UserSession
                ai.await().answer(1L, question)
                    // TODO: Handle errors gracefully
                    .collect { outgoing.send(Frame.Text(it)) }
                outgoing.send(Frame.Text("### END ###"))
            }
    }
}

