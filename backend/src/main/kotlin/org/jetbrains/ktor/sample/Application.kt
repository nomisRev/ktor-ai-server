package org.jetbrains.ktor.sample

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.config.property
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.routing.routing
import io.ktor.server.sessions.SessionTransportTransformerEncrypt
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.maxAge
import io.ktor.server.sse.SSE
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.util.hex
import kotlin.time.Duration.Companion.minutes
import org.jetbrains.ktor.sample.admin.installAdminRoutes
import org.jetbrains.ktor.sample.booking.installBookingRoutes
import org.jetbrains.ktor.sample.booking.installCustomerRoutes
import org.jetbrains.ktor.sample.chat.UserSession
import org.jetbrains.ktor.sample.chat.installChatRoutes
import org.jetbrains.ktor.sample.config.AppConfig
import org.jetbrains.ktor.sample.config.dependencies
import org.jetbrains.ktor.sample.security.configureOAuth
import org.jetbrains.ktor.sample.security.installAuthRoutes

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val config = property<AppConfig>("config")
    val module = dependencies(config)
    configure(config)
    routing {
        installAuthRoutes()
        installAdminRoutes(module.documentService)
        installChatRoutes(module.ai)
        installCustomerRoutes(module.customerRepository)
        installBookingRoutes(module.bookingRepository)
    }
}

private fun Application.configure(config: AppConfig) {
    configureOAuth(config.auth)
    install(ContentNegotiation) { json() }
    install(DefaultHeaders)
    install(CallLogging)
    install(SSE)
    install(WebSockets) { pingPeriod = 1.minutes }
    install(Sessions) {
        cookie<UserSession>("SESSION") {
            // TODO true for production
            cookie.secure = false
            cookie.extensions["SameSite"] = "lax"
            cookie.maxAge = 5.minutes
            cookie.httpOnly = true
            transform(
                SessionTransportTransformerEncrypt(
                    hex(config.auth.encryptionKey),
                    hex(config.auth.signKey),
                )
            )
        }
    }

    install(CORS) {
        // TODO: replace by correct domain(s) in production.
        allowHost("localhost:8000")

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Origin)
        allowHeader(HttpHeaders.Referrer)
        allowHeader("SESSION")
        exposeHeader("SESSION")
    }
}
