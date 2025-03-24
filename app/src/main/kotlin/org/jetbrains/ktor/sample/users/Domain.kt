package org.jetbrains.ktor.sample.users

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.ktor.sample.security.Role

@Serializable
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: Role,
    @SerialName("expires_at")
    val expiresAt: Instant
)

@Serializable
data class NewUser(
    val name: String,
    val password: Password,
    val email: String,
    val role: Role
)

@Serializable
data class UpdateUser(
    val name: String? = null,
    val password: Password? = null,
    val email: String? = null
)

@Serializable
@JvmInline
value class Password(val value: String) {
    override fun toString(): String = "<PASSWORD>"
}

@Serializable
data class Login(val username: String, val password: Password)

@Serializable
@JvmInline
value class Token(val value: String)
