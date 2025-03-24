package org.jetbrains.ktor.sample.users

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.ktor.sample.withApp
import org.junit.Test
import org.junit.jupiter.api.assertAll
import java.time.OffsetDateTime
import kotlin.test.assertNotNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
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
        assert(HttpStatusCode.Created == response.status)
        assert(expected == user)
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
        assert(HttpStatusCode.OK == loginResponse.status)
        assertNotNull(token, "Token should not be null")
    }

    @Test
    fun `login user with invalid credentials`() = withApp {
        val user = createUser(newTestUser())

        val loginResponse = post("/users/login") {
            setBody(Login(user.name, Password("wrongpassword")))
            contentType(ContentType.Application.Json)
        }

        assert(HttpStatusCode.Unauthorized == loginResponse.status)
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

        assert(HttpStatusCode.OK == updateResponse.status)
        val updated = updateResponse.body<User>()
        assertAll(
            { assert(updatedName == updated.name) { "Name should be updated" } },
            { assert(user.email == updated.email) { "Email should remain unchanged" } },
            { assert(user.role == updated.role) { "Role should remain unchanged" } }
        )
    }

    @Test
    fun `logout user`() = withApp {
        val (_, token) = createUserWithToken()

        val logoutResponse = post("/users/logout") {
            bearerAuth(token.value)
            contentType(ContentType.Application.Json)
        }

        assert(HttpStatusCode.OK == logoutResponse.status)

        val updateResponse = put("/users") {
            bearerAuth(token.value)
            contentType(ContentType.Application.Json)
            setBody(UpdateUser(name = "Updated after logout"))
        }

        assert(HttpStatusCode.Unauthorized == updateResponse.status)
    }

    @Test
    fun `create user twice returns conflict`() = withApp {
        val newUser = newTestUser()
        val createResponse = post("/users") {
            setBody(newUser)
            contentType(ContentType.Application.Json)
        }
        assert(HttpStatusCode.Created == createResponse.status)

        val duplicateResponse = post("/users") {
            setBody(newUser)
            contentType(ContentType.Application.Json)
        }
        assert(HttpStatusCode.Conflict == duplicateResponse.status)
        assert("User already exists" == duplicateResponse.body<String>())
    }

    @Test
    fun `create user twice with same username returns conflict`() = withApp {
        val newUser = newTestUser()
        val createResponse = post("/users") {
            setBody(newUser)
            contentType(ContentType.Application.Json)
        }
        assert(HttpStatusCode.Created == createResponse.status)

        val sameNameUser = newUser.copy(email = "${Uuid.random()}@jb.com")
        val sameNameResponse = post("/users") {
            setBody(sameNameUser)
            contentType(ContentType.Application.Json)
        }
        assert(HttpStatusCode.Conflict == sameNameResponse.status)
        assert("User already exists" == sameNameResponse.body<String>())
    }

    @Test
    fun `create user twice with same email returns conflict`() = withApp {
        val newUser = newTestUser()
        val createResponse = post("/users") {
            setBody(newUser)
            contentType(ContentType.Application.Json)
        }
        assert(HttpStatusCode.Created == createResponse.status)

        val sameEmailUser = newUser.copy(name = "${Uuid.random()}")
        val sameEmailResponse = post("/users") {
            setBody(sameEmailUser)
            contentType(ContentType.Application.Json)
        }
        assert(HttpStatusCode.Conflict == sameEmailResponse.status)
        assert("User already exists" == sameEmailResponse.body<String>())
    }
}
