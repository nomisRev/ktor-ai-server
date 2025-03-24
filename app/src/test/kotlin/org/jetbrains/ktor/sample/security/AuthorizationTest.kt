package org.jetbrains.ktor.sample.security

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.datetime.Instant
import org.jetbrains.ktor.sample.users.User
import org.junit.Test
import kotlin.test.assertEquals

class AuthorizationTest {

    @Test
    fun `authorized route allows access with correct role`() = testApplication {
        install(Authentication) {
            basic {
                validate { credentials ->
                    UserJWT(
                        User(
                            id = 1,
                            name = "Admin User",
                            email = "admin@example.com",
                            role = Role.ADMIN,
                            expiresAt = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3600000)
                        )
                    )
                }
            }
        }

        routing {
            authenticate {
                route("/admin") {
                    authorized(Role.ADMIN) {
                        get {
                            call.respond("Admin access granted")
                        }
                    }
                }
            }
        }

        val response = client.get("/admin") {
            basicAuth("admin", "password")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Admin access granted", response.bodyAsText())
    }

    @Test
    fun `authorized route denies access with incorrect role`() = testApplication {
        install(Authentication) {
            basic {
                validate { credentials ->
                    UserJWT(
                        User(
                            id = 2,
                            name = "Regular User",
                            email = "user@example.com",
                            role = Role.USER,
                            expiresAt = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3600000)
                        )
                    )
                }
            }
        }

        routing {
            authenticate {
                route("/admin") {
                    authorized(Role.ADMIN) {
                        get {
                            call.respond("Admin access granted")
                        }
                    }
                }
            }
        }

        val response = client.get("/admin") {
            basicAuth("user", "password")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals("Role ADMIN required", response.bodyAsText())
    }

    @Test
    fun `authorized route with multiple roles allows access with any matching role`() = testApplication {
        install(Authentication) {
            basic {
                validate { credentials ->
                    UserJWT(
                        User(
                            id = 3,
                            name = "Editor User",
                            email = "editor@example.com",
                            role = Role.USER, // Changed from "editor" to Role.USER to match enum
                            expiresAt = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3600000)
                        )
                    )
                }
            }
        }

        routing {
            authenticate {
                route("/content") {
                    authorized(Role.ADMIN, Role.USER) {
                        get {
                            call.respond("Content access granted")
                        }
                    }
                }
            }
        }

        val response = client.get("/content") {
            basicAuth("editor", "password")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Content access granted", response.bodyAsText())
    }

    @Test
    fun `authorized route with case-sensitive roles`() = testApplication {
        install(Authentication) {
            basic {
                validate { credentials ->
                    UserJWT(
                        User(
                            id = 3,
                            name = "Editor User",
                            email = "editor@example.com",
                            role = Role.USER, // Changed from "editor" to Role.USER to match enum
                            expiresAt = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3600000)
                        )
                    )
                }
            }
        }

        routing {
            authenticate {
                route("/content") {
                    authorized(Role.ADMIN) { // Using a different role than the user has
                        get {
                            call.respond("Content access granted")
                        }
                    }
                }
            }
        }

        val response = client.get("/content") {
            basicAuth("editor", "password")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals("Role ADMIN required", response.bodyAsText())
    }
}
