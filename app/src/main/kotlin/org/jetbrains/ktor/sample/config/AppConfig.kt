package org.jetbrains.ktor.sample.config

import io.ktor.server.application.ApplicationEnvironment
import org.jetbrains.ktor.sample.security.JWTConfig
import org.jetbrains.ktor.sample.users.Argon2HasherConfig

data class AppConfig(
    val jwt: JWTConfig,
    val database: DatabaseConfig,
    val ai: AIConfig,
    val flyway: FlywayConfig,
    val argon2: Argon2HasherConfig
) {
    companion object {
        fun load(environment: ApplicationEnvironment): AppConfig =
            AppConfig(
                jwt = JWTConfig.load(environment),
                database = DatabaseConfig.load(environment),
                ai = AIConfig.load(environment),
                flyway = FlywayConfig.load(environment),
                argon2 = Argon2HasherConfig.load(environment)
            )
    }
}
