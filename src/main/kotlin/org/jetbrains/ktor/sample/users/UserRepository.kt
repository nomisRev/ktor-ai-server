package org.jetbrains.ktor.sample.users

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.updateReturning

object Users : LongIdTable("users", "user_id") {
    val name = varchar("name", 50)
    val email = varchar("email", 100)
    val role = varchar("role", 50)
    val salt = binary("salt")
    val passwordHash = binary("password_hash")
    val expiresAt = timestamp("expires_at")
}

data class VerifyResult(val success: Boolean, val userId: Long)

// TODO: Question, should we use OAuth instead of manual user management for authentication with JWT?
class UserRepository(val database: Database, private val argon2Hasher: Argon2Hasher) {

    // TODO: Fix conflict
    suspend fun createUser(new: NewUser): User {
        val encrypted = argon2Hasher.encrypt(new.password)
        val now = Clock.System.now()
        return transaction(database) {
            val id = Users.insertAndGetId {
                it[name] = new.name
                it[email] = new.email
                it[role] = new.role
                it[salt] = encrypted.salt
                it[passwordHash] = encrypted.hash
                it[expiresAt] = now
            }.value
            User(id, new.name, new.email, new.role, now)
        }
    }

    // TODO: Fix user not found
    suspend fun verifyPassword(
        username: String,
        password: String
    ): VerifyResult {
        val (id, salt, hash) = transaction(database) {
            val row = Users.select(Users.id, Users.salt, Users.passwordHash)
                .where { Users.name eq username }
                .single()
            Triple(row[Users.id].value, row[Users.salt], row[Users.passwordHash])
        }
        return VerifyResult(argon2Hasher.verify(password, salt, hash), id)
    }

    private fun selectAll(): Query =
        Users.select(Users.id, Users.name, Users.email, Users.role, Users.expiresAt)

    fun getUserByIdOrNull(userId: Long): User? = transaction(database) {
        selectAll()
            .where { Users.id eq userId }
            .singleOrNull()
            ?.toUser()
    }

    suspend fun updateUserOrNull(userId: Long, update: UpdateUser): User? {
        val encrypted = update.password?.let { argon2Hasher.encrypt(it) }
        return transaction(database) {
            Users.updateReturning(where = { Users.id eq userId }) {
                if (update.name != null) it[name] = update.name
                if (update.email != null) it[email] = update.email
                if (update.role != null) it[role] = update.role
                if (encrypted != null) {
                    it[salt] = encrypted.salt
                    it[passwordHash] = encrypted.hash
                }
            }.singleOrNull()?.toUser()
        }
    }

    fun updateExpiresAt(userId: Long, expiresAt: Instant): Boolean =
        transaction(database) {
            Users.update({ Users.id eq userId }) {
                it[Users.expiresAt] = expiresAt
            } > 0
        }

    fun invalidateUserToken(user: User): Boolean =
        updateExpiresAt(user.id, Instant.fromEpochMilliseconds(0))

    fun deleteUser(userId: Long): Boolean = transaction(database) {
        Users.deleteWhere { id eq userId } > 0
    }

    fun ResultRow.toUser() = User(
        id = this[Users.id].value,
        name = this[Users.name],
        email = this[Users.email],
        role = this[Users.role],
        expiresAt = this[Users.expiresAt]
    )
}
