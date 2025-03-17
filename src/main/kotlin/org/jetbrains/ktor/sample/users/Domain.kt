package org.jetbrains.ktor.sample.users

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @field:Positive(message = "ID must be positive")
    val id: Long,

    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email cannot be blank")
    val email: String,

    @field:NotBlank(message = "Role cannot be blank")
    val role: String,

    @SerialName("expires_at")
    val expiresAt: Instant
)

@Serializable
data class NewUser(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    val password: String,

    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email cannot be blank")
    val email: String,

    @field:NotBlank(message = "Role cannot be blank")
    val role: String
)

@Serializable
data class UpdateUser(
    @field:NotBlank(message = "Name cannot be blank", groups = [OnUpdate::class])
    val name: String? = null,

    @field:Size(min = 8, message = "Password must be at least 8 characters long", groups = [OnUpdate::class])
    val password: String? = null,

    @field:Email(message = "Invalid email format", groups = [OnUpdate::class])
    @field:NotBlank(message = "Email cannot be blank", groups = [OnUpdate::class])
    val email: String? = null,

    @field:NotBlank(message = "Role cannot be blank", groups = [OnUpdate::class])
    val role: String? = null
)

// Validation group for update operations
interface OnUpdate

@Serializable
data class Login(
    @field:NotBlank(message = "Username cannot be blank")
    val username: String, 

    @field:NotBlank(message = "Password cannot be blank")
    val password: String
)

@Serializable
@JvmInline
value class Token(
    @field:NotBlank(message = "Token value cannot be blank")
    val value: String
)
