package org.jetbrains.ktor.sample

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.ktor.sample.users.UserRepository
import org.jetbrains.ktor.sample.users.Users
import kotlin.test.BeforeTest

abstract class DatabaseSpec {
    lateinit var database: Database

    @BeforeTest
    fun setup() {
        val dbConfig = PostgresContainer.getDatabaseConfig()
        database = Database.connect(
            url = dbConfig.jdbcUrl,
            driver = dbConfig.driverClassName,
            user = dbConfig.username,
            password = dbConfig.password
        )
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }
}