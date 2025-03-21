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
                            role = "admin",
                            expiresAt = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3600000)
                        )
                    )
                }
            }
        }

        routing {
            authenticate {
                route("/admin") {
                    authorized("admin") {
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
                            role = "user",
                            expiresAt = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3600000)
                        )
                    )
                }
            }
        }

        routing {
            authenticate {
                route("/admin") {
                    authorized("admin") {
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
        assertEquals("Role admin required", response.bodyAsText())
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
                            role = "editor",
                            expiresAt = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3600000)
                        )
                    )
                }
            }
        }

        routing {
            authenticate {
                route("/content") {
                    authorized("admin", "editor") {
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
                            role = "editor",
                            expiresAt = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3600000)
                        )
                    )
                }
            }
        }

        routing {
            authenticate {
                route("/content") {
                    authorized("EDITOR") {
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
        assertEquals("Role EDITOR required", response.bodyAsText())
    }
}
