package org.jetbrains.ktor.sample

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.ktor.sample.ai.AiRepo
import org.jetbrains.ktor.sample.ai.memory.ExposedChatMemoryStore
import org.jetbrains.ktor.sample.users.UserRepository
import org.jetbrains.ktor.sample.users.installUserRoutes
import org.jetbrains.ktor.sample.validation.JakartaValidation
import java.io.File

fun main(args: Array<String>) =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val appConfig = AppConfig.load(environment)
    val database = setupDatabase(appConfig.database, appConfig.flyway)
    val users = UserRepository(database)
    val ai = AiRepo(appConfig.ai, ExposedChatMemoryStore(database))

    configureJWT(appConfig.jwt, users)
    install(ContentNegotiation) { json() }
    install(JakartaValidation)

    routing {
        installUserRoutes(appConfig.jwt, users)
    }
}
