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
import io.ktor.server.sessions.maxAge
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import org.jetbrains.ktor.sample.admin.installAdminRoutes
import org.jetbrains.ktor.sample.chat.ChatSession
import org.jetbrains.ktor.sample.chat.installChatRoutes
import org.jetbrains.ktor.sample.security.configureJWT
import org.jetbrains.ktor.sample.config.AppConfig
import org.jetbrains.ktor.sample.config.dependencies
import org.jetbrains.ktor.sample.users.installUserRoutes
import kotlin.time.Duration.Companion.minutes

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
    install(Sessions) {
        cookie<ChatSession>("SESSION") {
            cookie.secure = true
            cookie.extensions["SameSite"] = "lax"
            cookie.maxAge = 5.minutes
        }
    }

    routing {
        installUserRoutes(module.users, module.jwtService)
        installAdminRoutes(module.documentService)
        installChatRoutes(module.ai)
    }
}
