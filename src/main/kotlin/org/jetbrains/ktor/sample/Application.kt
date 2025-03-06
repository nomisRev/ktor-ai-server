package org.jetbrains.ktor.sample

import org.jetbrains.ktor.sample.database.setupDatabase
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*

fun main(args: Array<String>) =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val appConfig = AppConfig.load(environment)
    val database = setupDatabase(appConfig.database)
    configureJWT(appConfig.jwt)
    install(ContentNegotiation) {
        json()
    }
    configureRouting()
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, "Hello, World!")
        }
    }
}
