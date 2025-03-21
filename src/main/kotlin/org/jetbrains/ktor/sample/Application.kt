package org.jetbrains.ktor.sample

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import org.jetbrains.ktor.sample.admin.installAdminRoutes
import org.jetbrains.ktor.sample.ai.installAiRoutes
import org.jetbrains.ktor.sample.security.configureJWT
import org.jetbrains.ktor.sample.config.AppConfig
import org.jetbrains.ktor.sample.config.dependencies
import org.jetbrains.ktor.sample.users.installUserRoutes

fun main(args: Array<String>) =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val config = AppConfig.load(environment)
    val module = dependencies(config)

    configureJWT(config.jwt, module.jwtService)
    install(ContentNegotiation) { json() }

    routing {
        installUserRoutes(module.users, module.jwtService)
        installAdminRoutes(module.ai)
        installAiRoutes(module.ai)
    }
}
