package org.jetbrains.ktor.sample

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.test.TestResult
import kotlinx.serialization.json.Json
import io.ktor.server.config.mergeWith

fun withApp(test: suspend HttpClient.() -> Unit): TestResult =
    testApplication {
        environment {
            config = ApplicationConfig("application.yaml")
                .mergeWith(PostgresContainer.getDatabaseConfig())
        }
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
        startApplication()
        test(client)
    }