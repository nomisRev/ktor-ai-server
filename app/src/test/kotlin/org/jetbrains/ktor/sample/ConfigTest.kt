package org.jetbrains.ktor.sample

import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.jetbrains.ktor.sample.config.DatabaseConfig
import kotlin.test.Test

class ConfigTest {
    @Test
    fun `test loading database configuration`() {
        val environment = createTestEnvironment {
            config = MapApplicationConfig(
                "database.host" to "localhost",
                "database.port" to "5432",
                "database.name" to "testdb",
                "database.username" to "test-user",
                "database.password" to "test-password",
                "database.driverClassName" to "org.driver.TestDriver",
                "database.maxPoolSize" to "-1",
                "database.cachePrepStmts" to "true",
                "database.prepStmtCacheSize" to "250",
                "database.prepStmtCacheSqlLimit" to "2048",
            )
        }

        assert(
            DatabaseConfig(
                host = "localhost",
                port = 5432,
                name = "testdb",
                username = "test-user",
                password = "test-password",
                driverClassName = "org.driver.TestDriver",
                maxPoolSize = -1,
                cachePrepStmts = true,
                prepStmtCacheSize = 250,
                prepStmtCacheSqlLimit = 2048,
            ) == DatabaseConfig.load(environment)
        )
    }
}
