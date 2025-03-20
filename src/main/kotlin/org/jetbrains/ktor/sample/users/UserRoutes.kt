package org.jetbrains.ktor.sample.users

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.auth.principal
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.jetbrains.ktor.sample.auth.JWTService
import org.jetbrains.ktor.sample.auth.UserJWT

fun Routing.installUserRoutes(repository: UserRepository, jwtService: JWTService) {
    route("/users") {
        post {
            val new = call.receive<NewUser>()
            val user = repository.createUser(new)
            call.respond(status = HttpStatusCode.Created, user)
        }
        post("/login") {
            val login = call.receive<Login>()
            val result = repository.verifyPassword(login.username, login.password)
            if (result.success) {
                val token = jwtService.generateToken(result.userId)
                call.respond(HttpStatusCode.OK, Token(token))
            } else {
                call.respond(status = HttpStatusCode.Unauthorized, message = "Invalid username or password")
            }
        }

        authenticate {
            put {
                val jwt = call.principal<UserJWT>()!!
                val updatedUser = call.receive<UpdateUser>()
                val updated = repository.updateUserOrNull(jwt.user.id, updatedUser)
                if (updated != null) call.respond(HttpStatusCode.OK, updated)
                else call.respond(HttpStatusCode.NotFound, "User not found")
            }

            post("/logout") {
                val jwt = call.principal<UserJWT>()!!
                val success = repository.invalidateUserToken(jwt.user)
                if (success) call.respond(HttpStatusCode.OK, "Logged out successfully")
                else call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }
    }
}
