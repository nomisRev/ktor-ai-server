package org.jetbrains.ktor.sample.config

import kotlinx.serialization.Serializable
import org.jetbrains.ktor.sample.security.AuthConfig

@Serializable
data class AppConfig(
    val auth: AuthConfig,
    val database: DatabaseConfig,
    val ai: AIConfig,
    val flyway: FlywayConfig,
)
