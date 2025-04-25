package org.jetbrains.ktor.sample

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.mergeWith
import io.ktor.server.testing.testApplication

fun withApp(test: suspend HttpClient.() -> Unit) = testApplication {
    environment {
        config =
            ApplicationConfig("application.yaml").mergeWith(PostgresContainer.getMapAppConfig())
    }
    val client = createClient { install(ContentNegotiation) { json() } }
    startApplication()
    try {
        test(client)
    } finally {
        client.close()
    }
}
