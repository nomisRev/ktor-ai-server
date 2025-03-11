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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserRoutesTest {
    @Test
    fun `create user`() = withApp {
        val user = newTestUser()
        val response = post("/users") {
            setBody(user)
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val actual = response.body<User>()
        assertEquals(user.name, actual.name)
        assertEquals(user.email, actual.email)
        assertEquals(user.role, actual.role)
    }

    @Test
    fun `login user`() = withApp {
        val user = newTestUser()
        val createResponse = post("/users") {
            setBody(user)
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)

        val loginResponse = post("/users/login") {
            setBody(Login(user.name, user.password))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val token = loginResponse.body<Token>().token
        assertNotNull(token, "Token should not be null")
    }

    @Test
    fun `login user with invalid credentials`() = withApp {
        val user = newTestUser()
        val createResponse = post("/users") {
            setBody(user)
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)

        val loginResponse = post("/users/login") {
            setBody(Login(user.name, "wrongpassword"))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.Unauthorized, loginResponse.status)
    }

    @Test
    fun `update user`() = withApp {
        val user = newTestUser()
        val createResponse = post("/users") {
            setBody(user)
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val createdUser = createResponse.body<User>()

        val loginResponse = post("/users/login") {
            setBody(Login(user.name, user.password))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val token = loginResponse.body<Token>().token
        assertNotNull(token, "Token should not be null")

        val updatedName = "Updated ${user.name}"
        val updateResponse = put("/users/${createdUser.id}") {
            setBody(UpdateUser(
                id = createdUser.id,
                name = updatedName,
                email = null,
                password = null,
                role = null,
                expiresAt = null
            ))
            contentType(ContentType.Application.Json)
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val updatedUser = updateResponse.body<User>()
        assertEquals(updatedName, updatedUser.name)
        assertEquals(createdUser.email, updatedUser.email)
        assertEquals(createdUser.role, updatedUser.role)
    }

    @Test
    fun `update non-existent user`() = withApp {
        val user = newTestUser()
        val createResponse = post("/users") {
            setBody(user)
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)

        val loginResponse = post("/users/login") {
            setBody(Login(user.name, user.password))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val token = loginResponse.body<Token>().token
        assertNotNull(token, "Token should not be null")

        val nonExistentUserId = Long.MIN_VALUE
        val updateResponse = put("/users/$nonExistentUserId") {
            setBody(UpdateUser(
                id = nonExistentUserId,
                name = "Non-existent User",
                email = null,
                password = null,
                role = null,
                expiresAt = null
            ))
            contentType(ContentType.Application.Json)
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.BadRequest, updateResponse.status)
    }

    @Test
    fun `update user with invalid userId`() = withApp {
        val user = newTestUser()
        val createResponse = post("/users") {
            setBody(user)
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val createdUser = createResponse.body<User>()

        val loginResponse = post("/users/login") {
            setBody(Login(user.name, user.password))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val token = loginResponse.body<Token>().token
        assertNotNull(token, "Token should not be null")

        val updateResponse = put("/users/invalid") {
            setBody(UpdateUser(
                id = createdUser.id,
                name = "Updated with invalid userId",
                email = null,
                password = null,
                role = null,
                expiresAt = null
            ))
            contentType(ContentType.Application.Json)
            bearerAuth(token)
        }
        
        assertEquals(HttpStatusCode.BadRequest, updateResponse.status)
    }
}
