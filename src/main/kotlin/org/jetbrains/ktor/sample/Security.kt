package org.jetbrains.ktor.sample

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respond
import org.jetbrains.ktor.sample.users.User
import org.jetbrains.ktor.sample.users.UserRepository
import java.util.Date

data class UserJWT(val user: User)

fun Application.configureJWT(jwtConfig: JWTConfig, users: UserRepository) {
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
                // TODO clean-up code
                val userId = credential.getClaim("user_id", Long::class)
                val now = Date()
                when {
                    userId == null ->
                        respond(HttpStatusCode.Unauthorized, "Invalid token")

                    credential.expiresAt?.before(now) == true ->
                        respond(HttpStatusCode.Unauthorized, "Token has expired")

                    else -> when (val user = users.getUserById(userId)) {
                        null -> respond(HttpStatusCode.Unauthorized)
                        else ->
                            if (Date(user.expiresAt.toEpochMilliseconds()).before(now)) {
                                respond(HttpStatusCode.Unauthorized, "Token has expired")
                            } else UserJWT(user)
                    }
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
