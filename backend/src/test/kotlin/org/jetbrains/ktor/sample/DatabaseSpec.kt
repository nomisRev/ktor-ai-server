package org.jetbrains.ktor.sample

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.ktor.sample.config.dataSource
import org.jetbrains.ktor.sample.config.flyway
import org.junit.AfterClass
import org.junit.ClassRule

abstract class DatabaseSpec {
    companion object {
        @JvmStatic
        @get:ClassRule
        val dataSource =
            dataSource(AppTestConfig.database).also { flyway(it, AppTestConfig.flyway) }

        @JvmStatic @get:ClassRule val database = Database.connect(dataSource)

        @AfterClass
        @JvmStatic
        fun closeDataSource() {
            TransactionManager.closeAndUnregister(database)
            dataSource.close()
        }
    }
}
