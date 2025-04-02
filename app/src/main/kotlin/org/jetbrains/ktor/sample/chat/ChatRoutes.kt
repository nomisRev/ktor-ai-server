package org.jetbrains.ktor.sample.chat

import io.ktor.http.HttpStatusCode
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sse.sse
import io.ktor.server.websocket.webSocket
import io.ktor.sse.ServerSentEvent
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
    get("/") {
        val hasSession = call.sessions.get<UserSession>() != null
        val redirectUrl = if (hasSession) "/home" else "login"
        call.respondRedirect(redirectUrl)
    }

    staticResources("/", "web")
    staticResources("/login", "web")
    staticResources("/home", "web") {
        modify { _, call ->
            if (call.sessions.get<UserSession>() == null) call.respondRedirect("/login")
        }
    }

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

    sse("/chat") {
        val session = call.sessions.get<UserSession>() ?: return@sse call.respond(HttpStatusCode.Unauthorized)
        val question = call.request.queryParameters["question"] ?: return@sse call.respond(HttpStatusCode.BadRequest)
        // TODO: Replace with memoryId / userId from UserSession
        ai.await().answer(1L, question)
            .collect { token -> send(ServerSentEvent(token)) }
    }
}
