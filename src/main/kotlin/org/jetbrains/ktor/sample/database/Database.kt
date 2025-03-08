package org.jetbrains.ktor.sample.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.ktor.sample.DatabaseConfig
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun Application.setupDatabase(config: DatabaseConfig): Database {
    val config = HikariConfig().apply {
        jdbcUrl = config.jdbcUrl
        username = config.username
        password = config.password
        driverClassName = config.driverClassName
        maximumPoolSize = config.maxPoolSize
        addDataSourceProperty("cachePrepStmts", "true")
        addDataSourceProperty("prepStmtCacheSize", "250")
        addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    }
    val database = Database.connect(HikariDataSource(config))
    // TODO: replace with FlyWay
    transaction(database) { SchemaUtils.create(Users) }
    // When the application stops, we close all connections and unregister from TransactionManager
    monitor.subscribe(ApplicationStopped) { TransactionManager.closeAndUnregister(database) }
    return database
}
