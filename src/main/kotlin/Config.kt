package com.example

import io.ktor.server.application.ApplicationEnvironment

data class JWTConfig(
    val domain: String,
    val audience: String,
    val realm: String,
    val secret: String
)

data class AppConfig(val jwt: JWTConfig) {
    companion object {
        fun load(environment: ApplicationEnvironment): AppConfig {
            val jwtConfig = JWTConfig(
                domain = environment.config.property("jwt.domain").getString(),
                audience = environment.config.property("jwt.audience").getString(),
                realm = environment.config.property("jwt.realm").getString(),
                secret = environment.config.property("jwt.secret").getString()
            )
            return AppConfig(jwt = jwtConfig)
        }
    }
}
