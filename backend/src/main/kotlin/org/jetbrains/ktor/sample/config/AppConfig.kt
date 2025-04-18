package org.jetbrains.ktor.sample.config

import io.ktor.server.application.ApplicationEnvironment
import org.jetbrains.ktor.sample.security.AuthConfig

data class AppConfig(
    val auth: AuthConfig,
    val database: DatabaseConfig,
    val ai: AIConfig,
    val flyway: FlywayConfig,
) {
    companion object {
        fun load(environment: ApplicationEnvironment): AppConfig =
            AppConfig(
                auth = AuthConfig.load(environment),
                database = DatabaseConfig.load(environment),
                ai = AIConfig.load(environment),
                flyway = FlywayConfig.load(environment),
            )
    }
}
