package org.jetbrains.ktor.sample

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import io.ktor.server.config.MapApplicationConfig
import org.testcontainers.containers.wait.strategy.Wait
import javax.sql.DataSource

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
//                withCommand("postgres -c shared_preload_libraries=vector")
                start()
            }
    }

    fun getMapAppConfig() =
        MapApplicationConfig().apply {
            put("database.host", container.host)
            put("database.port", container.firstMappedPort.toString())
            put("database.name", container.databaseName)
            put("database.username", container.username)
            put("database.password", container.password)
            put("database.driverClassName", container.driverClassName)
            put("database.maxPoolSize", "5")
            put("database.cachePrepStmts", "true")
            put("database.prepStmtCacheSize", "250")
            put("database.prepStmtCacheSqlLimit", "2048")
            put("flyway.enabled", "true")
            put("flyway.locations", "classpath:db/migration")
            put("flyway.baselineOnMigrate", "true")
        }

    fun getFlywayConfig() =
        FlywayConfig(
            locations = "classpath:db/migration",
            baselineOnMigrate = true
        )

    fun getDatabaseConfig() =
        DatabaseConfig(
            driverClassName = container.driverClassName,
            host = container.host,
            port = container.firstMappedPort,
            name = container.databaseName,
            username = container.username,
            password = container.password,
            maxPoolSize = 5,
            cachePrepStmts = true,
            prepStmtCacheSize = 250,
            prepStmtCacheSqlLimit = 2048,
        )
}
