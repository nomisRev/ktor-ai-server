package org.jetbrains.ktor.sample

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import io.ktor.server.config.mergeWith

fun withApp(test: suspend HttpClient.() -> Unit) =
    testApplication {
        environment {
            config = ApplicationConfig("application.yaml")
                .mergeWith(PostgresContainer.getMapAppConfig())
        }
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        startApplication()
        test(client)
    }
