package org.jetbrains.ktor.sample.users

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.jetbrains.ktor.sample.withApp
import org.junit.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserRoutesTest {
    @Test
    fun `create user`() = withApp {
        val newUser = newTestUser()
        val response = post("/users") {
            setBody(newUser)
            contentType(ContentType.Application.Json)
        }
        val user = response.body<User>()
        val expected = User(
            id = user.id,
            name = newUser.name,
            email = newUser.email,
            role = newUser.role,
            expiresAt = user.expiresAt
        )
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(expected, user)
    }

    @Test
    fun `login user`() = withApp {
        val newUser = newTestUser()
        val user = createUser(newUser)

        val loginResponse = post("/users/login") {
            setBody(Login(user.name, newUser.password))
            contentType(ContentType.Application.Json)
        }

        val token = loginResponse.body<Token>().value
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        assertNotNull(token, "Token should not be null")
    }

    @Test
    fun `login user with invalid credentials`() = withApp {
        val user = createUser(newTestUser())

        val loginResponse = post("/users/login") {
            setBody(Login(user.name, "wrongpassword"))
            contentType(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.Unauthorized, loginResponse.status)
    }

    @Test
    fun `update user`() = withApp {
        val (user, token) = createUserWithToken()
        val updatedName = "Updated ${user.name}"

        val updateResponse = put("/users") {
            bearerAuth(token.value)
            contentType(ContentType.Application.Json)
            setBody(
                UpdateUser(
                    name = updatedName
                )
            )
        }

        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val updated = updateResponse.body<User>()
        assertAll(
            { assertEquals(updatedName, updated.name, "Name should be updated") },
            { assertEquals(user.email, updated.email, "Email should remain unchanged") },
            { assertEquals(user.role, updated.role, "Role should remain unchanged") }
        )
    }

    @Test
    fun `logout user`() = withApp {
        val (_, token) = createUserWithToken()

        val logoutResponse = post("/users/logout") {
            bearerAuth(token.value)
            contentType(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, logoutResponse.status)

        val updateResponse = put("/users") {
            bearerAuth(token.value)
            contentType(ContentType.Application.Json)
            setBody(UpdateUser(name = "Updated after logout"))
        }

        assertEquals(HttpStatusCode.Unauthorized, updateResponse.status)
    }
}
