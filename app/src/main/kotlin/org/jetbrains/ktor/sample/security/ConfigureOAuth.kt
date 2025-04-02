package org.jetbrains.ktor.sample.security

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.*

data class OAuthConfig(
    val realm: String,
    val authorizeUrl: String,
    val accessTokenUrl: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUrl: String
) {
    companion object {
        fun load(environment: ApplicationEnvironment): OAuthConfig {
            val config = environment.config.config("oauth")
            return OAuthConfig(
                realm = config.property("realm").getString(),
                authorizeUrl = config.property("authorizeUrl").getString(),
                accessTokenUrl = config.property("accessTokenUrl").getString(),
                clientId = config.property("clientId").getString(),
                clientSecret = config.property("clientSecret").getString(),
                redirectUrl = config.property("redirectUrl").getString()
            )
        }
    }
}

fun Application.configureOAuth(config: OAuthConfig) {
    val httpClient = HttpClient(Apache) {
        install(ContentNegotiation) { json() }
    }
    monitor.subscribe(ApplicationStopped) { httpClient.close() }
    authentication {
        oauth("auth-oauth-keycloak") {
            urlProvider = { config.redirectUrl }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "keycloak",
                    authorizeUrl = config.authorizeUrl,
                    accessTokenUrl = config.accessTokenUrl,
                    requestMethod = HttpMethod.Post,
                    clientId = config.clientId,
                    clientSecret = config.clientSecret,
                    defaultScopes = listOf("openid", "profile", "email")
                )
            }
            client = httpClient
        }
    }
}
