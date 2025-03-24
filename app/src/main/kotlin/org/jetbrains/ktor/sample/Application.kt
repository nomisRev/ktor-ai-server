package org.jetbrains.ktor.sample

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.routing.routing
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.util.generateNonce
import org.jetbrains.ktor.sample.admin.installAdminRoutes
import org.jetbrains.ktor.sample.ai.installAiRoutes
import org.jetbrains.ktor.sample.chat.ChatSession
import org.jetbrains.ktor.sample.chat.installChatRoutes
import org.jetbrains.ktor.sample.security.configureJWT
import org.jetbrains.ktor.sample.config.AppConfig
import org.jetbrains.ktor.sample.config.dependencies
import org.jetbrains.ktor.sample.users.installUserRoutes
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun main(args: Array<String>) =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val config = AppConfig.load(environment)
    val module = dependencies(config)

    configureJWT(config.jwt, module.jwtService)
    install(ContentNegotiation) { json() }
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets) { pingPeriod = 1.minutes }
    install(Sessions) { cookie<ChatSession>("SESSION") }
    intercept(ApplicationCallPipeline.Plugins) {
        if (call.sessions.get<ChatSession>() == null) {
            @OptIn(ExperimentalUuidApi::class)
            call.sessions.set(ChatSession("1"))
        }
    }

    routing {
        installUserRoutes(module.users, module.jwtService)
        installAdminRoutes(module.ai)
        installAiRoutes(module.ai)
        installChatRoutes()
    }
}
