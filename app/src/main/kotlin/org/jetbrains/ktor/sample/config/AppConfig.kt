package org.jetbrains.ktor.sample.config

import io.ktor.server.application.ApplicationEnvironment
import org.jetbrains.ktor.sample.security.OAuthConfig

data class AppConfig(
    val oauth: OAuthConfig,
    val database: DatabaseConfig,
    val ai: AIConfig,
    val flyway: FlywayConfig
) {
    companion object {
        fun load(environment: ApplicationEnvironment): AppConfig =
            AppConfig(
                oauth = OAuthConfig.load(environment),
                database = DatabaseConfig.load(environment),
                ai = AIConfig.load(environment),
                flyway = FlywayConfig.load(environment)
            )
    }
}
