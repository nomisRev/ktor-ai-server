package org.jetbrains.ktor.sample

import org.jetbrains.ktor.sample.database.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.ktor.sample.database.UserEntity
import kotlin.test.*


class DatabaseTest {
    @Test
    fun testDatabaseConnection() = withApp {
        transaction {
            val id = Users.insert {
                it[name] = "Test User"
                it[email] = "test@example.com"
            }[Users.id]

            val user = UserEntity.findById(id)!!.toUser()

            assertEquals("Test User", user.name, "User name should match")
            assertEquals("test@example.com", user.email, "User email should match")
        }
    }
}
