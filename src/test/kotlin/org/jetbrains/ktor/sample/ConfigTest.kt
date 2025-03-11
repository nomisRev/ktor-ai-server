package org.jetbrains.ktor.sample

import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigTest {
    @Test
    fun `test loading configuration`() {
        val environment = createTestEnvironment {
            config = MapApplicationConfig(
                "jwt.issuer" to "https://test-auth-domain/",
                "jwt.audience" to "test-audience",
                "jwt.realm" to "test realm",
                "jwt.secret" to "test-secret"
            )
        }

        assertEquals(
            JWTConfig(
                issuer = "https://test-auth-domain/",
                audience = "test-audience",
                realm = "test realm",
                secret = "test-secret"
            ),
            JWTConfig.load(environment)
        )
    }

    @Test
    fun `test loading database configuration`() {
        val environment = createTestEnvironment {
            config = MapApplicationConfig(
                "database.jdbcUrl" to "jdbc:postgresql://localhost/testdb",
                "database.username" to "test-user",
                "database.password" to "test-password",
                "database.driverClassName" to "org.driver.TestDriver",
                "database.maxPoolSize" to "-1",
                "database.cachePrepStmts" to "true",
                "database.prepStmtCacheSize" to "250",
                "database.prepStmtCacheSqlLimit" to "2048",
            )
        }
        
        assertEquals(
            DatabaseConfig(
                jdbcUrl = "jdbc:postgresql://localhost/testdb",
                username = "test-user",
                password = "test-password",
                driverClassName = "org.driver.TestDriver",
                maxPoolSize = -1,
                cachePrepStmts = true,
                prepStmtCacheSize = 250,
                prepStmtCacheSqlLimit = 2048,
            ),
            DatabaseConfig.load(environment)
        )
    }
}
