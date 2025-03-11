package org.jetbrains.ktor.sample.users

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(Users)

    var name by Users.name
    var email by Users.email
    var role by Users.role
    var salt by Users.salt
    var passwordHash by Users.passwordHash
    var expiresAt by Users.expiresAt

    fun toUser(): User = User(
        id = id.value,
        name = name,
        email = email,
        role = role,
        expiresAt = expiresAt
    )
}

class UserEntityRepository(private val database: Database, private val encryption: Encryption = Encryption()) {

    suspend fun createUser(newUser: NewUser): User =
        newSuspendedTransaction(Dispatchers.IO, database) {
            val (salt, hash) = encryption.encrypt(newUser.password)
            UserEntity.new {
                this.name = newUser.name
                this.email = newUser.email
                this.role = newUser.role
                this.salt = salt
                this.passwordHash = hash
                this.expiresAt = newUser.expiresAt
            }.toUser()
        }

    suspend fun verifyPassword(
        username: String,
        password: String
    ): Boolean =
        newSuspendedTransaction(Dispatchers.IO, database) {
            val user = UserEntity.find { Users.name eq username }.single()
            encryption.verify(password, user.salt, user.passwordHash)
        }

    suspend fun getUserById(id: Int): User? = newSuspendedTransaction(Dispatchers.IO, database) {
        UserEntity.findById(id)?.toUser()
    }

    suspend fun getUserByEmail(email: String): User? = newSuspendedTransaction(Dispatchers.IO, database) {
        UserEntity.find { Users.email eq email }.firstOrNull()?.toUser()
    }

    // TODO: Should this use the new upsert operation if user doesn't exist?
    suspend fun updateUser(updateUser: UpdateUser): User? = newSuspendedTransaction(Dispatchers.IO, database) {
        val entity = UserEntity.findById(updateUser.id)
        if (updateUser.name != null) entity?.name = updateUser.name
        if (updateUser.email != null) entity?.email = updateUser.email
        if (updateUser.role != null) entity?.role = updateUser.role
        if (updateUser.expiresAt != null) entity?.expiresAt = updateUser.expiresAt
        // Handle password update if provided
        if (updateUser.password != null) {
            val (salt, hash) = encryption.encrypt(updateUser.password)
            entity?.salt = salt
            entity?.passwordHash = hash
        }
        entity?.toUser()
    }

    suspend fun deleteUser(id: Int): Boolean = newSuspendedTransaction(Dispatchers.IO, database) {
        UserEntity.findById(id)?.delete() != null
    }

    suspend fun getAllUsers(): List<User> = newSuspendedTransaction(Dispatchers.IO, database) {
        UserEntity.all().map(UserEntity::toUser)
    }
}
