package com.example

import com.example.database.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*


class DatabaseTest {
    @Test
    fun testDatabaseConnection() = withApp {
        transaction {
            val id = Users.insert {
                it[name] = "Test User"
                it[email] = "test@example.com"
            }[Users.id]

            val userRow = Users.select(Users.id, Users.name, Users.email)
                .where { Users.id eq id }
                .single()

            assertEquals("Test User", userRow[Users.name], "User name should match")
            assertEquals("test@example.com", userRow[Users.email], "User email should match")
        }
    }
}
