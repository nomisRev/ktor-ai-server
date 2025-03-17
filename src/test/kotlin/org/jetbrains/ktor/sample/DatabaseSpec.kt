package org.jetbrains.ktor.sample

import org.jetbrains.exposed.sql.Database
import kotlin.test.BeforeTest

abstract class DatabaseSpec {
    lateinit var database: Database

    @BeforeTest
    fun setup() {
        val config = PostgresContainer.getDatabaseConfig()
        val dataSource = dataSource(config)
        flyway(dataSource, PostgresContainer.getFlywayConfig())
        database = Database.connect(dataSource)
    }
}
