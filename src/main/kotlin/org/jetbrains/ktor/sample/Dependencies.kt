package org.jetbrains.ktor.sample

import io.ktor.server.application.Application
import org.jetbrains.ktor.sample.ai.AiRepo
import org.jetbrains.ktor.sample.ai.ExposedChatMemoryStore
import org.jetbrains.ktor.sample.users.Argon2Hasher
import org.jetbrains.ktor.sample.auth.JWTService
import org.jetbrains.ktor.sample.users.UserRepository

class Dependencies(
    val users: UserRepository,
    val jwtService: JWTService,
    val ai: AiRepo
)

fun Application.dependencies(config: AppConfig): Dependencies {
    val database = setupDatabase(config.database, config.flyway)
    val users = UserRepository(database, Argon2Hasher(config.argon2))

    return Dependencies(
        users = users,
        jwtService = JWTService(config.jwt, users),
        ai = AiRepo(config.ai, ExposedChatMemoryStore(database))
    )
}