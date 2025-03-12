package org.jetbrains.ktor.sample.users

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.auth.principal
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.datetime.Instant
import org.jetbrains.ktor.sample.JWTConfig
import org.jetbrains.ktor.sample.UserNameJWT
import java.time.Duration
import java.util.Date


private val ONE_HOUR =
    Duration.ofHours(1).toMillis()

fun Routing.installUserRoutes(config: JWTConfig, repository: UserRepository) {
    route("/users") {
        post {
            val new = call.receive<NewUser>()
            val user = repository.createUser(new)
            call.respond(status = HttpStatusCode.Created, user)
        }
        post("/login") {
            val login = call.receive<Login>()
            // TODO move to service
            val result = repository.verifyPassword(login.username, login.password)
            if (result.success) {
                val issuedAt = System.currentTimeMillis()
                val expiresAt = issuedAt + ONE_HOUR
                repository.updateExpiresAt(result.userId, Instant.fromEpochMilliseconds(expiresAt))
                val token = JWT.create()
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .withClaim("user_id", result.userId)
                    .withExpiresAt(Date(expiresAt))
                    .withIssuedAt(Date(issuedAt))
                    .sign(Algorithm.HMAC256(config.secret))
                call.respond(Token(token))
            } else {
                call.respond(status = HttpStatusCode.Unauthorized, message = "Invalid username or password")
            }
        }

        authenticate {
            // TODO rely on userId from JWT to eliminate ability to update not-yourself
            //  that is only allowed for ADMIN role
            put {
                val jwt = call.principal<UserNameJWT>()!!
                val updatedUser = call.receive<UpdateUser>()
                val updated = repository.updateUser(jwt.user.id, updatedUser)
                if (updated != null) call.respond(HttpStatusCode.OK, updated)
                else call.respond(HttpStatusCode.NotFound, "User not found")
            }

            post("/logout") {
                val jwt = call.principal<UserNameJWT>()!!
                val success = repository.invalidateUserToken(jwt.user)
                if (success) call.respond(HttpStatusCode.OK, "Logged out successfully")
                else call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }
    }
}
