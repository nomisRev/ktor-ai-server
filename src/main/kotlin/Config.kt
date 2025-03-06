package com.example

import io.ktor.server.application.ApplicationEnvironment

data class JWTConfig(
    val domain: String,
    val audience: String,
    val realm: String,
    val secret: String
)

data class DatabaseConfig(
    val driverClassName: String,
    val jdbcURL: String,
    val username: String,
    val password: String,
    val maxPoolSize: Int
)

data class AppConfig(val jwt: JWTConfig, val database: DatabaseConfig) {
    companion object {
        fun load(environment: ApplicationEnvironment): AppConfig = with(environment.config) {
            val jwtConfig = JWTConfig(
                domain = property("jwt.domain").getString(),
                audience = property("jwt.audience").getString(),
                realm = property("jwt.realm").getString(),
                secret = property("jwt.secret").getString()
            )

            val databaseConfig = DatabaseConfig(
                driverClassName = property("database.driverClassName").getString(),
                jdbcURL = property("database.jdbcURL").getString(),
                username = property("database.username").getString(),
                password = property("database.password").getString(),
                maxPoolSize = property("database.maxPoolSize").getString().toInt()
            )

            AppConfig(jwt = jwtConfig, database = databaseConfig)
        }
    }
}
