package org.jetbrains.ktor.sample

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun Application.setupDatabase(config: DatabaseConfig, flywayConfig: FlywayConfig): Database {
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.name}"
        username = config.username
        password = config.password
        driverClassName = config.driverClassName
        maximumPoolSize = config.maxPoolSize
        addDataSourceProperty("cachePrepStmts", config.cachePrepStmts.toString())
        addDataSourceProperty("prepStmtCacheSize", config.prepStmtCacheSize.toString())
        addDataSourceProperty("prepStmtCacheSqlLimit", config.prepStmtCacheSqlLimit.toString())
    })

    Flyway.configure()
        .dataSource(dataSource)
        .locations(flywayConfig.locations)
        .load()
        .migrate()

    val database = Database.connect(dataSource)

    monitor.subscribe(ApplicationStopped) {
        TransactionManager.closeAndUnregister(database)
        dataSource.close()
    }

    return database
}
