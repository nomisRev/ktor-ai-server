package org.jetbrains.ktor.sample

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.ktor.sample.users.Users
import kotlin.test.BeforeTest

abstract class DatabaseSpec {
    lateinit var database: Database

    @BeforeTest
    fun setup() {
        val config = PostgresContainer.getDatabaseConfig()
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
        database = Database.connect(dataSource)
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }
}
