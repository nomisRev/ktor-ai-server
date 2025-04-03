package org.jetbrains.ktor.sample.security

import com.auth0.jwt.JWT
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.OAuthAccessTokenResponse.OAuth2
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import org.jetbrains.ktor.sample.chat.UserSession
import java.time.Instant

fun Routing.installAuthRoutes() {
    authenticate("auth-oauth-keycloak") {
        get("/login") {
            // The OAuth plugin will intercept this request and redirect to Keycloak
            // No need to do anything here
        }

        get("/callback") {
            val principal: OAuth2? = call.authentication.principal()
            if (principal == null) call.respond(Unauthorized)
            else {
                val userSession = UserSession(JWT.decode(principal.accessToken).subject)
                call.sessions.set(userSession)
                call.respondRedirect("/")
            }
        }
    }
}
