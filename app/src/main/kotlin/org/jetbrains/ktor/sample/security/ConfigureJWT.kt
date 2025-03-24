package org.jetbrains.ktor.sample.security

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respond

fun Application.configureJWT(jwtConfig: JWTConfig, jwt: JWTService) {
    authentication {
        jwt {
            realm = jwtConfig.realm
            verifier(jwt.verifier)
            validate { credential -> jwt.validateToken(credential) }
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
