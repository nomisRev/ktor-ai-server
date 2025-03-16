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
    val host: String,
    val port: Int,
    val name: String,
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
                host = property("database.host").getString(),
                port = property("database.port").getString().toInt(),
                name = property("database.name").getString(),
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

data class FlywayConfig(
    val locations: String,
    val baselineOnMigrate: Boolean
) {
    companion object {
        fun load(environment: ApplicationEnvironment): FlywayConfig = with(environment.config) {
            FlywayConfig(
                locations = property("flyway.locations").getString(),
                baselineOnMigrate = property("flyway.baselineOnMigrate").getString().toBoolean()
            )
        }
    }
}

data class AppConfig(val jwt: JWTConfig, val database: DatabaseConfig, val ai: AIConfig, val flyway: FlywayConfig) {
    companion object {
        fun load(environment: ApplicationEnvironment): AppConfig =
            AppConfig(
                jwt = JWTConfig.load(environment), 
                database = DatabaseConfig.load(environment),
                ai = AIConfig.load(environment),
                flyway = FlywayConfig.load(environment)
            )
    }
}
