package org.jetbrains.ktor.sample.admin

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.jetbrains.ktor.sample.security.Role
import org.jetbrains.ktor.sample.users.Login
import org.jetbrains.ktor.sample.users.NewUser
import org.jetbrains.ktor.sample.users.Token
import org.jetbrains.ktor.sample.users.User
import org.jetbrains.ktor.sample.withApp
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
class AdminRoutesTest {

    private fun newAdminUser(): NewUser {
        val random = Uuid.Companion.random()
        return NewUser(
            name = "$random Admin",
            email = "$random@example.com",
            role = Role.ADMIN,
            password = "password"
        )
    }

    private fun newRegularUser(): NewUser {
        val random = Uuid.Companion.random()
        return NewUser(
            name = "$random User",
            email = "$random@example.com",
            role = Role.USER,
            password = "password"
        )
    }

    @Test
    fun `admin can upload document`() = withApp {
        val adminUser = post("/users") {
            setBody(newAdminUser())
            contentType(ContentType.Application.Json)
        }.body<User>()

        val token = post("/users/login") {
            setBody(Login(adminUser.name, "password"))
            contentType(ContentType.Application.Json)
        }.body<Token>()

        val response = post("/admin/documents/upload") {
            bearerAuth(token.value)
            contentType(ContentType.Application.Json)
            setBody(DocumentUpload("This is a test document content"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `non-admin cannot upload document`() = withApp {
        val regularUser = post("/users") {
            setBody(newRegularUser())
            contentType(ContentType.Application.Json)
        }.body<User>()

        val token = post("/users/login") {
            setBody(Login(regularUser.name, "password"))
            contentType(ContentType.Application.Json)
        }.body<Token>()

        val response = post("/admin/documents/upload") {
            bearerAuth(token.value)
            contentType(ContentType.Application.Json)
            setBody(DocumentUpload("This is a test document content"))
        }

        assertEquals(HttpStatusCode.Companion.Forbidden, response.status)
        assertEquals("Role ADMIN required", response.bodyAsText())
    }

    @Test
    fun `unauthenticated user cannot upload document`() = withApp {
        val response = post("/admin/documents/upload") {
            bearerAuth("poop")
            contentType(ContentType.Application.Json)
            setBody(DocumentUpload("This is a test document content"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
