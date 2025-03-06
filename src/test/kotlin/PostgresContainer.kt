package com.example

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import io.ktor.server.config.MapApplicationConfig
import org.testcontainers.containers.wait.strategy.Wait

object PostgresContainer {
    /**
     * At the end of the testsuite the Ryuk container started by Testcontainers will stop the container.
     * https://java.testcontainers.org/test_framework_integration/manual_lifecycle_control/
     */
    private val container: PostgreSQLContainer<Nothing> by lazy {
        PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:16-alpine"))
            .apply {
                withDatabaseName("ktor_sample")
                withUsername("ktor_user")
                withPassword("<PASSWORD>")
                waitingFor(Wait.forListeningPort())
                start()
            }
    }

    fun getDatabaseConfig() =
        MapApplicationConfig().apply {
            put("database.jdbcUrl", container.jdbcUrl)
            put("database.username", container.username)
            put("database.password", container.password)
            put("database.driverClassName", container.driverClassName)
            put("database.maxPoolSize", "5")
        }
}
