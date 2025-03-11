package org.jetbrains.ktor.sample.users

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.updateReturning

object Users : LongIdTable("users", "user_id") {
    val name = varchar("name", 50)
    val email = varchar("email", 100)
    val role = varchar("role", 50)
    val salt = binary("salt")
    val passwordHash = binary("password_hash")
    val expiresAt = timestamp("expires_at")
}

/** This class is build _without_ Exposed DAO module, it relies on the SQL DSL only. */
class UserRepository(val database: Database, private val argon2Hasher: Argon2Hasher = Argon2Hasher()) {

    suspend fun createUser(new: NewUser): User = withContext(Dispatchers.IO) {
        val encrypted = argon2Hasher.encrypt(new.password)
        newSuspendedTransaction(Dispatchers.IO, database) {
            val id = Users.insertAndGetId {
                it[name] = new.name
                it[email] = new.email
                it[role] = new.role
                it[salt] = encrypted.salt
                it[passwordHash] = encrypted.hash
                it[expiresAt] = new.expiresAt
            }.value
            User(id, new.name, new.email, new.role, new.expiresAt)
        }
    }

    suspend fun verifyPassword(
        username: String,
        password: String
    ): Boolean = withContext(Dispatchers.IO) {
        val (salt, hash) = newSuspendedTransaction(Dispatchers.IO, database) {
            val row = Users.select(Users.salt, Users.passwordHash)
                .where { Users.name eq username }
                .single()
            Pair(row[Users.salt], row[Users.passwordHash])
        }
        argon2Hasher.verify(password, salt, hash)
    }

    private fun selectAll(): Query =
        Users.select(Users.id, Users.name, Users.email, Users.role, Users.expiresAt)

    suspend fun getUserById(userId: Long): User? = newSuspendedTransaction(Dispatchers.IO, database) {
        selectAll()
            .where { Users.id eq userId }
            .singleOrNull()
            ?.toUser()
    }

    suspend fun getUserByEmail(email: String): User? = newSuspendedTransaction(Dispatchers.IO, database) {
        selectAll()
            .where { Users.email eq email }
            .firstOrNull()
            ?.toUser()
    }

    suspend fun updateUser(update: UpdateUser): User? = withContext(Dispatchers.IO) {
        val encrypted = update.password?.let { argon2Hasher.encrypt(it) }
        newSuspendedTransaction(Dispatchers.IO, database) {
            Users.updateReturning(where = { Users.id eq update.id }) {
                if (update.name != null) it[name] = update.name
                if (update.email != null) it[email] = update.email
                if (update.role != null) it[role] = update.role
                if (update.expiresAt != null) it[expiresAt] = update.expiresAt
                if (encrypted != null) {
                    it[salt] = encrypted.salt
                    it[passwordHash] = encrypted.hash
                }
            }.singleOrNull()?.toUser()
        }
    }

    suspend fun deleteUser(userId: Long): Boolean = newSuspendedTransaction(Dispatchers.IO, database) {
        Users.deleteWhere { id eq userId } > 0
    }

    suspend fun getAllUsers(): List<User> = newSuspendedTransaction(Dispatchers.IO, database) {
        selectAll().map { it.toUser() }
    }

    fun ResultRow.toUser() = User(
        id = this[Users.id].value,
        name = this[Users.name],
        email = this[Users.email],
        role = this[Users.role],
        expiresAt = this[Users.expiresAt]
    )
}
