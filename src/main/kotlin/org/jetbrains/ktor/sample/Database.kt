package org.jetbrains.ktor.sample

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.ktor.sample.users.Users

fun Application.setupDatabase(config: DatabaseConfig): Database {
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = config.jdbcUrl
        username = config.username
        password = config.password
        driverClassName = config.driverClassName
        maximumPoolSize = config.maxPoolSize
        addDataSourceProperty("cachePrepStmts", "true")
        addDataSourceProperty("prepStmtCacheSize", "250")
        addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    })
    val database = Database.connect(dataSource)
    // TODO: replace with FlyWay
    transaction(database) {
        SchemaUtils.drop(Users)
        SchemaUtils.create(Users)
    }
    // When the application stops, we close all connections and unregister from TransactionManager
    monitor.subscribe(ApplicationStopped) {
        TransactionManager.closeAndUnregister(database)
        dataSource.close()
    }
    return database
}
