package org.jetbrains.ktor.sample.validation

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.application.install
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable
import org.jetbrains.ktor.sample.validation.JakartaValidationPluginTest.User
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class JakartaValidationPluginTest {

    @Serializable
    data class User(
        @field:NotBlank(message = "Name cannot be blank")
        val name: String,

        @field:Email(message = "Invalid email format")
        val email: String,

        @field:Min(value = 18, message = "Age must be at least 18")
        val age: Int,

        @field:Size(min = 8, message = "Password must be at least 8 characters long")
        val password: String
    )

    @Test
    fun `test valid`() = withServer {
        val user = User("John Doe", "john.doe@example.com", 25, "password123")
        val response = post("/user") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }

        assert(HttpStatusCode.Created == response.status)
        val responseBody = response.body<User>()
        assert(responseBody == user)
    }

    @Test
    fun `test invalid request - blank name`() = withServer {
        val response = post("/user") {
            contentType(ContentType.Application.Json)
            setBody(User("", "john.doe@example.com", 25, "password123"))
        }

        assert(HttpStatusCode.BadRequest == response.status)
        val responseBody = response.bodyAsText()
        assert(responseBody == "Name cannot be blank")
    }

    @Test
    fun `test invalid request - invalid email`() = withServer {
        val response = post("/user") {
            contentType(ContentType.Application.Json)
            setBody(User("John Doe", "invalid-email", 25, "password123"))
        }

        assert(HttpStatusCode.BadRequest == response.status)
        assert(response.bodyAsText() == "Invalid email format")
    }

    @Test
    fun `test invalid request - age below minimum`() = withServer {
        val response = post("/user") {
            contentType(ContentType.Application.Json)
            setBody(User("John Doe", "john.doe@example.com", 16, "password123"))
        }

        assert(HttpStatusCode.BadRequest == response.status)
        assert(response.bodyAsText() == "Age must be at least 18")
    }

    @Test
    fun `test invalid request - password too short`() = withServer {
        val response = post("/user") {
            contentType(ContentType.Application.Json)
            setBody(User("John Doe", "john.doe@example.com", 25, "short"))
        }

        assert(HttpStatusCode.BadRequest == response.status)
        assert(response.bodyAsText() == "Password must be at least 8 characters long")
    }

    @Test
    fun `test multiple validation errors`() = withServer {
        val response = post("/user") {
            contentType(ContentType.Application.Json)
            setBody(User("", "invalid-email", 16, "short"))
        }

        assert(HttpStatusCode.BadRequest == response.status)
        val message = response.bodyAsText()
        assertContains(message, "Invalid email format")
        assertContains(message, "Age must be at least 18")
        assertContains(message, "Password must be at least 8 characters long")
        assertContains(message, "Name cannot be blank")
    }

    @Test
    fun `test custom error handler`() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            install(JakartaValidation) {
                errorHandler { errors ->
                    respond(HttpStatusCode.BadRequest, "Custom error: ${errors.joinToString { it.message }}")
                }
            }

            routing {
                post("/user") {
                    val user = call.receive<User>()
                    call.respond(HttpStatusCode.Created, user)
                }
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) { json() }
        }

        val response = client.post("/user") {
            contentType(ContentType.Application.Json)
            setBody(User("", "john.doe@example.com", 25, "password123"))
        }

        assert(HttpStatusCode.BadRequest == response.status)
        val responseBody = response.bodyAsText()
        assert(responseBody == "Custom error: Name cannot be blank")
    }
}

private fun withServer(block: suspend HttpClient.() -> Unit) = testApplication {
    application {
        install(ContentNegotiation) { json() }
        install(JakartaValidation)

        routing {
            post("/user") {
                val user = call.receive<User>()
                call.respond(HttpStatusCode.Created, user)
            }
        }
    }
    block(createClient {
        install(ClientContentNegotiation) { json() }
    })
}
