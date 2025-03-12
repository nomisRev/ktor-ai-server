package org.jetbrains.ktor.sample.users

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    @SerialName("expires_at")
    val expiresAt: Instant
)

@Serializable
data class NewUser(
    val name: String,
    val password: String,
    val email: String,
    val role: String
)

@Serializable
data class UpdateUser(
    val name: String? = null,
    val password: String? = null,
    val email: String? = null,
    val role: String? = null
)


@Serializable
data class Login(val username: String, val password: String)

@Serializable
@JvmInline
value class Token(val value: String)
