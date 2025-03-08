package org.jetbrains.ktor.sample.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Instant

class UserRepository(private val database: Database) {

    fun createUser(name: String, email: String, role: String, expiresAt: Instant): User = transaction(database) {
        UserEntity.new {
            this.name = name
            this.email = email
            this.role = role
            this.expiresAt = expiresAt
        }.toUser()
    }

    fun getUserById(id: Int): User? = transaction(database) {
        UserEntity.findById(id)?.toUser()
    }

    fun getUserByEmail(email: String): User? = transaction(database) {
        UserEntity.find { Users.email eq email }.firstOrNull()?.toUser()
    }

    fun updateUser(id: Int, name: String? = null, email: String? = null, role: String? = null, expiresAt: Instant? = null): User? = transaction(database) {
        UserEntity.findById(id)?.also { user ->
            name?.let { user.name = it }
            email?.let { user.email = it }
            role?.let { user.role = it }
            expiresAt?.let { user.expiresAt = it }
        }?.toUser()
    }

    fun deleteUser(id: Int): Boolean = transaction(database) {
        val user = UserEntity.findById(id) ?: return@transaction false
        user.delete()
        true
    }

    fun getAllUsers(): List<User> = transaction(database) {
        UserEntity.all().map { it.toUser() }
    }
}
