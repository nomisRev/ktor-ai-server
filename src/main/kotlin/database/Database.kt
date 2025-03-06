package com.example.database

import com.example.DatabaseConfig
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun Application.setupDatabase(config: DatabaseConfig): Database {
    val database = Database.connect(
        url = config.jdbcURL,
        driver = config.driverClassName,
        user = config.username,
        password = config.password
    )
    // TODO: replace with FlyWay
    transaction(database) { SchemaUtils.create(Users) }
    // When the application stops, we close all connections and unregister from TransactionManager
    monitor.subscribe(ApplicationStopped) { TransactionManager.closeAndUnregister(database) }
    return database
}
