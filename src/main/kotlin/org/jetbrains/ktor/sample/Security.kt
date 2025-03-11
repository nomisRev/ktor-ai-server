package org.jetbrains.ktor.sample

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respond
import java.util.Date

fun Application.configureJWT(jwtConfig: JWTConfig) {
    authentication {
        jwt {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                // TODO: We should validate the user hasn't logged out or token was invalidated
                //   check matching expiresAt in database?
                when {
                    credential.expiresAt?.before(Date()) == true ->
                        respond(HttpStatusCode.Unauthorized, "Token has expired")
                    else -> JWTPrincipal(credential.payload)
                }
            }
            challenge { defaultScheme, realm ->
                val message = when {
                    call.request.headers["Authorization"].isNullOrBlank() -> "Missing or empty Authorization header"
                    else -> "Token is not valid or has expired"
                }
                call.respond(HttpStatusCode.Unauthorized, message)
            }
        }
    }
}
