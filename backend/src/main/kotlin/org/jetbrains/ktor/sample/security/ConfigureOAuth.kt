package org.jetbrains.ktor.sample.security

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.*
import kotlinx.serialization.Serializable

@Serializable
data class AuthConfig(
    val authorizeUrl: String,
    val accessTokenUrl: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUrl: String,
    val encryptionKey: String,
    val signKey: String,
)

fun Application.configureOAuth(config: AuthConfig) {
    val httpClient = HttpClient(Apache) { install(ContentNegotiation) { json() } }
    monitor.subscribe(ApplicationStopped) { httpClient.close() }
    authentication {
        oauth {
            urlProvider = { config.redirectUrl }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = config.authorizeUrl,
                    accessTokenUrl = config.accessTokenUrl,
                    requestMethod = HttpMethod.Post,
                    clientId = config.clientId,
                    clientSecret = config.clientSecret,
                    defaultScopes = listOf("openid", "profile", "email"),
                )
            }
            client = httpClient
        }
    }
}
