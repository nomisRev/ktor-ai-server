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
import org.jetbrains.ktor.sample.ai.AiRepo

@Serializable
data class ChatSession(val id: Int)

fun Routing.installChatRoutes(ai: Deferred<AiRepo>) {
    staticResources("", "web")

//    authenticate {
    webSocket("/ws") {
        val session = call.sessions.get<ChatSession>() ?: ChatSession(1)
//        if (session == null) return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
//            val principal = call.principal<UserJWT>()!!
        send(Frame.Text("Hey, I am your personal travel assistant. How may I help you today?"))
        incoming.consumeAsFlow()
            .filterIsInstance<Frame.Text>()
            .collect { frame ->
                val question = frame.readText()
                val answer = ai.await().answer(session.id.toLong(), question)
                send(Frame.Text(answer))
            }
    }
//    }
}

