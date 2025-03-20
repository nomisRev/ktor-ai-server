package org.jetbrains.ktor.sample

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.ApplicationStopped
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun Application.setupDatabase(config: DatabaseConfig, flywayConfig: FlywayConfig): Database {
    val dataSource = dataSource(config)
    flyway(dataSource, flywayConfig)
    val database = Database.connect(dataSource)

    monitor.subscribe(ApplicationStopped) {
        TransactionManager.closeAndUnregister(database)
        dataSource.close()
    }

    return database
}

fun flyway(dataSource: HikariDataSource, flywayConfig: FlywayConfig): MigrateResult =
    Flyway.configure()
        .dataSource(dataSource)
        .locations(flywayConfig.locations)
        .baselineOnMigrate(true)
        .load()
        .migrate()

fun dataSource(config: DatabaseConfig): HikariDataSource =
    HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.name}"
        username = config.username
        password = config.password
        driverClassName = config.driverClassName
        maximumPoolSize = config.maxPoolSize
        addDataSourceProperty("cachePrepStmts", config.cachePrepStmts.toString())
        addDataSourceProperty("prepStmtCacheSize", config.prepStmtCacheSize.toString())
        addDataSourceProperty("prepStmtCacheSqlLimit", config.prepStmtCacheSqlLimit.toString())
    })

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
