package org.jetbrains.ktor.sample

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import org.jetbrains.ktor.sample.auth.configureJWT
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
        put("/ai") {
            val question = call.queryParameters["question"]!!
            val response = module.ai.await().answer(1, question)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
