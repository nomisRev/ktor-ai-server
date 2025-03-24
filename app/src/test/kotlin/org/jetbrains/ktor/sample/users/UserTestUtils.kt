package org.jetbrains.ktor.sample.users

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.jetbrains.ktor.sample.security.Role
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun newTestUser(): NewUser {
    val random = Uuid.random()
    return NewUser(
        name = "$random User",
        email = "$random@example.com",
        role = Role.USER,
        password = Password("password")
    )
}

suspend fun HttpClient.createUser(newUser: NewUser): User =
    post("/users") {
        setBody(newUser)
        contentType(ContentType.Application.Json)
    }.body<User>()

suspend fun HttpClient.login(user: User, password: Password): Token =
    post("/users/login") {
        setBody(Login(user.name, password))
        contentType(ContentType.Application.Json)
    }.body<Token>()

suspend fun HttpClient.createUserWithToken(): Pair<User, Token> {
    val newUser = newTestUser()
    val user = createUser(newUser)
    val token = login(user, newUser.password)
    return Pair(user, token)
}
