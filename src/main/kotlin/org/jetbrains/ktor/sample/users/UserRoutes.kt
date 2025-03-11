package org.jetbrains.ktor.sample.users

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.jetbrains.ktor.sample.JWTConfig
import java.time.Duration
import java.util.Date
import kotlin.text.toLong

private val ONE_HOUR =
    Duration.ofHours(1).toMillis()

fun Application.installUserRoutes(config: JWTConfig, repository: UserRepository) {
    routing {
        route("/users") {
            post {
                val new = call.receive<NewUser>()
                val user = repository.createUser(new)
                call.respond(status = HttpStatusCode.Created, user)
            }
            post("/login") {
                val user = call.receive<Login>()
                // TODO move to service
                if (repository.verifyPassword(user.username, user.password)) {
                    val token = JWT.create()
                        .withAudience(config.audience)
                        .withIssuer(config.issuer)
                        .withClaim("username", user.username)
                        .withExpiresAt(Date(System.currentTimeMillis() + ONE_HOUR))
                        .sign(Algorithm.HMAC256(config.secret))
                    call.respond(Token(token))
                } else {
                    call.respond(status = HttpStatusCode.Unauthorized, message = "Invalid username or password")
                }
            }

            authenticate {
                put("/{userId}") {
                    val userId = call.parameters["userId"]?.toIntOrNull()
                    val updatedUser = call.receive<UpdateUser>()
                    if (userId != null) {
                        val updated = repository.updateUser(updatedUser)
                        if (updated != null) call.respond(status = HttpStatusCode.OK, updated)
                        else call.respond(status = HttpStatusCode.NotFound, message = "User $userId not found")
                    } else {
                        call.respond(status = HttpStatusCode.BadRequest, message = "Invalid or missing userId")
                    }
                }
            }
        }
    }
}
