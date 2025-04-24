package org.jetbrains.ktor.sample.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import kotlinx.serialization.Serializable
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun Application.setupDatabase(config: DatabaseConfig, flywayConfig: FlywayConfig): Database {
    val dataSource = dataSource(config)
    flyway(dataSource, flywayConfig)
    val database =
        Database.connect(
            dataSource,
            databaseConfig =
                org.jetbrains.exposed.sql.DatabaseConfig {
                    // TODO: Configure alert in Prometheus/Grafana
                    warnLongQueriesDuration = 1000
                },
        )

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
    HikariDataSource(
        HikariConfig().apply {
            jdbcUrl =
                "jdbc:postgresql://${config.host}:${config.port}/${config.name}?sslmode=require"
            username = config.username
            password = config.password
            driverClassName = config.driverClassName
            maximumPoolSize = config.maxPoolSize
            addDataSourceProperty("cachePrepStmts", config.cachePrepStmts.toString())
            addDataSourceProperty("prepStmtCacheSize", config.prepStmtCacheSize.toString())
            addDataSourceProperty("prepStmtCacheSqlLimit", config.prepStmtCacheSqlLimit.toString())
        }
    )

@Serializable
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
)

@Serializable data class FlywayConfig(val locations: String, val baselineOnMigrate: Boolean)
