package org.jetbrains.ktor.sample

import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.maxAge
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import kotlinx.datetime.Clock
import org.jetbrains.ktor.sample.chat.UserSession
import org.jetbrains.ktor.sample.chat.installChatRoutes
import org.jetbrains.ktor.sample.config.AppConfig
import org.jetbrains.ktor.sample.config.dependencies
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun main(args: Array<String>) =
    io.ktor.server.netty.EngineMain.main(args)

@OptIn(ExperimentalUuidApi::class)
fun Application.module() {
    val config = AppConfig.load(environment)
    val module = dependencies(config)

//    TODO: replace with OAuth2
//    configureJWT(config.jwt, module.jwtService)
    install(ContentNegotiation) { json() }
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets) { pingPeriod = 1.minutes }
    install(Sessions) {
        cookie<UserSession>("SESSION") {
            cookie.secure = true
            cookie.extensions["SameSite"] = "lax"
            cookie.maxAge = 5.minutes
            cookie.httpOnly = true
        }
    }
    // TODO: Temporary until OAuth2 is added
    intercept(ApplicationCallPipeline.Plugins) {
        if (call.sessions.get<UserSession>() == null) {
            call.sessions.set(
                UserSession(
                    Uuid.random().toString(),
                    Uuid.random().toString(),
                    (Clock.System.now() + 1.hours).epochSeconds,
                )
            )
        }
    }

//    install(CORS) {
//        allowMethod(HttpMethod.Get)
//        allowMethod(HttpMethod.Post)
//        allowMethod(HttpMethod.Put)
//        // @TODO: Don't do this in production if possible. Try to limit it.
//        allowHost("localhost:8081")
//    }

    routing {
//        installUserRoutes(module.users, module.jwtService)
//        installAdminRoutes(module.documentService)
        installChatRoutes(module.ai)
    }
}
