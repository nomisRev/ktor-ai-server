package org.jetbrains.ktor.sample.database

import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Users : IntIdTable("users", "user_id") {
    val name = varchar("name", 50)
    val email = varchar("email", 100)
    val role = varchar("role", 50)
    val expiresAt = timestamp("expires_at")
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(Users)

    var name by Users.name
    var email by Users.email
    var role by Users.role
    var expiresAt by Users.expiresAt

    fun toUser(): User = User(
        id = id.value,
        name = name,
        email = email,
        role = role,
        expiresAt = expiresAt
    )
}

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val expiresAt: Instant?
)
