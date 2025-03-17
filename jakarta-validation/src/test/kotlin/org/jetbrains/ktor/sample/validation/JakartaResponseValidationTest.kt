package org.jetbrains.ktor.sample.validation

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.application.install
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable
import kotlin.test.Test

class JakartaResponseValidationTest {

    @Serializable
    data class ResponseUser(
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
    fun `test valid response`() = withResponseValidationServer {
        val response = get("/valid-user")

        assert(HttpStatusCode.OK == response.status)
        val user = response.body<ResponseUser>()
        assert("John Doe" == user.name)
        assert("john.doe@example.com" == user.email)
        assert(25 == user.age)
        assert("password123" == user.password)
    }

    @Test
    fun `test invalid response - blank name`() = withResponseValidationServer {
        val response = get("/invalid-user-blank-name")
        assert(HttpStatusCode.InternalServerError == response.status)
    }

    @Test
    fun `test invalid response - invalid email`() = withResponseValidationServer {
        val response = get("/invalid-user-email")
        assert(HttpStatusCode.InternalServerError == response.status)
    }

    @Test
    fun `test invalid response - age below minimum`() = withResponseValidationServer {
        val response = get("/invalid-user-age")
        assert(HttpStatusCode.InternalServerError == response.status)
    }

    @Test
    fun `test invalid response - password too short`() = withResponseValidationServer {
        val response = get("/invalid-user-password")
        assert(HttpStatusCode.InternalServerError == response.status)
    }

    @Test
    fun `test multiple validation errors in response`() = withResponseValidationServer {
        val response = get("/invalid-user-multiple")
        assert(HttpStatusCode.InternalServerError == response.status)
    }

    @Test
    fun `test custom error handler for response validation`() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            install(JakartaValidation) {
                responseErrorHandler { errors ->
                    respond(HttpStatusCode.BadRequest, "Custom error: ${errors.joinToString { it.message }}")
                }
            }

            routing {
                get("/invalid-user-blank-name") {
                    call.respond(ResponseUser("", "john.doe@example.com", 25, "password123"))
                }
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) { json() }
        }

        val response = client.get("/invalid-user-blank-name")

        assert(HttpStatusCode.BadRequest == response.status)
        val responseBody = response.bodyAsText()
        assert("Custom error: Name cannot be blank" == responseBody)
    }

    private fun withResponseValidationServer(block: suspend HttpClient.() -> Unit) = testApplication {
        application {
            install(ContentNegotiation) { json() }
            install(JakartaValidation)

            routing {
                get("/valid-user") {
                    call.respond(ResponseUser("John Doe", "john.doe@example.com", 25, "password123"))
                }
                get("/invalid-user-blank-name") {
                    call.respond(ResponseUser("", "john.doe@example.com", 25, "password123"))
                }
                get("/invalid-user-email") {
                    call.respond(ResponseUser("John Doe", "invalid-email", 25, "password123"))
                }
                get("/invalid-user-age") {
                    call.respond(ResponseUser("John Doe", "john.doe@example.com", 16, "password123"))
                }
                get("/invalid-user-password") {
                    call.respond(ResponseUser("John Doe", "john.doe@example.com", 25, "short"))
                }
                get("/invalid-user-multiple") {
                    call.respond(ResponseUser("", "invalid-email", 16, "short"))
                }
            }
        }
        block(createClient {
            install(ClientContentNegotiation) { json() }
        })
    }
}
