package org.jetbrains.ktor.sample.config

import io.ktor.server.application.Application
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jetbrains.ktor.sample.ai.AiMetrics
import org.jetbrains.ktor.sample.ai.AiRepo
import org.jetbrains.ktor.sample.ai.ExposedChatMemoryStore
import org.jetbrains.ktor.sample.users.Argon2Hasher
import org.jetbrains.ktor.sample.security.JWTService
import org.jetbrains.ktor.sample.users.UserRepository

class Dependencies(
    val users: UserRepository,
    val jwtService: JWTService,
    val ai: Deferred<AiRepo>
)

fun Application.dependencies(config: AppConfig): Dependencies {
    val database = setupDatabase(config.database, config.flyway)
    val registry = setupMetrics()
    val users = UserRepository(database, Argon2Hasher(config.argon2))
    return Dependencies(
        users = users,
        jwtService = JWTService(config.jwt, users),
        ai = async(Dispatchers.IO) { AiRepo(config.ai, ExposedChatMemoryStore(database), AiMetrics(registry)) }
    )
}
