package org.jetbrains.ktor.sample.auth

import io.ktor.server.application.ApplicationEnvironment

data class JWTConfig(
    val issuer: String,
    val audience: String,
    val realm: String,
    val secret: String
) {
    companion object {
        fun load(environment: ApplicationEnvironment): JWTConfig = with(environment.config) {
            JWTConfig(
                issuer = property("jwt.issuer").getString(),
                audience = property("jwt.audience").getString(),
                realm = property("jwt.realm").getString(),
                secret = property("jwt.secret").getString()
            )
        }
    }
}