package org.jetbrains.ktor.sample

import io.ktor.server.application.ApplicationEnvironment
import org.jetbrains.ktor.sample.ai.AIConfig

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

data class DatabaseConfig(
    val driverClassName: String,
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maxPoolSize: Int,
    val cachePrepStmts: Boolean,
    val prepStmtCacheSize: Int,
    val prepStmtCacheSqlLimit: Int,
) {
    companion object {
        fun load(environment: ApplicationEnvironment): DatabaseConfig = with(environment.config) {
            DatabaseConfig(
                driverClassName = property("database.driverClassName").getString(),
                jdbcUrl = property("database.jdbcUrl").getString(),
                username = property("database.username").getString(),
                password = property("database.password").getString(),
                maxPoolSize = property("database.maxPoolSize").getString().toInt(),
                cachePrepStmts = property("database.cachePrepStmts").getString().toBoolean(),
                prepStmtCacheSize = property("database.prepStmtCacheSize").getString().toInt(),
                prepStmtCacheSqlLimit = property("database.prepStmtCacheSqlLimit").getString().toInt(),
            )
        }
    }
}

data class AppConfig(val jwt: JWTConfig, val database: DatabaseConfig, val ai: AIConfig) {
    companion object {
        fun load(environment: ApplicationEnvironment): AppConfig =
            AppConfig(
                jwt = JWTConfig.load(environment), 
                database = DatabaseConfig.load(environment),
                ai = AIConfig.load(environment)
            )
    }
}
