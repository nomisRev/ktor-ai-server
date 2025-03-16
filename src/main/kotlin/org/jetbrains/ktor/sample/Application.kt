package org.jetbrains.ktor.sample

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import org.jetbrains.ktor.sample.users.UserRepository
import org.jetbrains.ktor.sample.users.installUserRoutes

fun main(args: Array<String>) =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val appConfig = AppConfig.load(environment)
    val database = setupDatabase(appConfig.database, appConfig.flyway)
    val users = UserRepository(database)

    configureJWT(appConfig.jwt, users)

    install(ContentNegotiation) { json() }

    routing {
        installUserRoutes(appConfig.jwt, users)
    }
}
