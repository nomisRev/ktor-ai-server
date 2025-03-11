package org.jetbrains.ktor.sample.database

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.ktor.sample.DatabaseSpec
import org.jetbrains.ktor.sample.PostgresContainer
import org.jetbrains.ktor.sample.users.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UserEntityRepositoryTest : DatabaseSpec() {
    private val userRepository by lazy { UserRepository(database) }

    @Test
    fun `test verifyPassword with correct password`() = runBlocking {
        // Create a test user
        val username = "testuser"
        val password = "correctpassword"
        val email = "test@example.com"
        val role = "USER"
        val expiresAt = Clock.System.now().plus(Duration.parse("P30D"))

        userRepository.createUser(NewUser(username, password, email, role, expiresAt))

        // Verify password with correct credentials
        val result = userRepository.verifyPassword(username, password)
        
        // Assert that verification succeeds
        assertTrue(result, "Password verification should succeed with correct password")
    }

    @Test
    fun `test verifyPassword with incorrect password`() = runBlocking {
        // Create a test user
        val username = "testuser2"
        val password = "correctpassword"
        val email = "test2@example.com"
        val role = "USER"
        val expiresAt = Clock.System.now().plus(30.days)

        userRepository.createUser(NewUser(username, password, email, role, expiresAt))

        // Verify password with incorrect credentials
        val result = userRepository.verifyPassword(username, "wrongpassword")
        
        // Assert that verification fails
        assertFalse(result, "Password verification should fail with incorrect password")
    }

    @Test
    fun `test verifyPassword with non-existent user`() = runBlocking {
        // Attempt to verify password for a non-existent user
        try {
            userRepository.verifyPassword("nonexistentuser", "anypassword")
            // If we reach here, the test should fail because an exception should have been thrown
            assertFalse(true, "verifyPassword should throw an exception for non-existent user")
        } catch (e: NoSuchElementException) {
            // Expected behavior - the function should throw NoSuchElementException
            assertTrue(true, "verifyPassword correctly throws exception for non-existent user")
        }
    }

    @Test
    fun testCreateAndGetUser() = runBlocking {
        val expiresAt = Clock.System.now().plus(1.hours)
        val user = userRepository.createUser(
            NewUser(
                name = "Test User",
                password = "password",
                email = "testCreateAndGetUser1@example.com",
                role = "USER",
                expiresAt = expiresAt
            )
        )
        val actualUser = User(user.id, "Test User", "testCreateAndGetUser1@example.com", "USER", expiresAt)
        assertEquals(actualUser, user)

        val retrievedUser = userRepository.getUserById(user.id)
        assertEquals(actualUser, retrievedUser)

        val retrievedByEmail = userRepository.getUserByEmail("testCreateAndGetUser1@example.com")
        assertEquals(actualUser, retrievedByEmail)
    }

    @Test
    fun testUpdateUser() = runBlocking {
        val expiresAt = Clock.System.now().plus(1.hours)
        val user = insertUser(expiresAt)
        val updatedUser = userRepository.updateUser(
            UpdateUser(
                id = user.id,
                name = "testUpdateUser User",
                password = null,
                email = "testUpdateUser@example.com",
                role = "ADMIN",
                expiresAt = expiresAt
            )
        )

        val actualUser = User(user.id, "testUpdateUser User", "testUpdateUser@example.com", "ADMIN", expiresAt)
        assertEquals(actualUser, updatedUser)
    }

    @Test
    fun testDeleteUser() = runBlocking {
        val user = insertUser(Clock.System.now())
        assertTrue(userRepository.deleteUser(user.id))
        val retrievedUser = userRepository.getUserById(user.id)
        assertNull(retrievedUser)
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun insertUser(instant: Instant): User {
        val random = Uuid.random()
        return userRepository.createUser(
            NewUser(
                name = "$random User",
                password = "password",
                email = "$random@example.com",
                role = "USER",
                expiresAt = instant
            )
        )
    }
}