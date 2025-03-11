package org.jetbrains.ktor.sample.database

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.ktor.sample.DatabaseSpec
import org.jetbrains.ktor.sample.users.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UserRepositoryTest : DatabaseSpec() {
    private val userRepository by lazy { UserRepository(database) }

    @Test
    fun `test verifyPassword with correct password`() = runBlocking {
        val user = insertUser()
        val result = userRepository.verifyPassword(user.name, "password")
        assertTrue(result, "Password verification should succeed with correct password")
    }

    @Test
    fun `test verifyPassword with incorrect password`() = runBlocking {
        val user = insertUser()
        val result = userRepository.verifyPassword(user.name, "wrongpassword")
        assertFalse(result, "Password verification should fail with incorrect password")
    }

    @Test
    fun `test verifyPassword with non-existent user`() = runBlocking {
        try {
            userRepository.verifyPassword("nonexistentuser", "password")
            assertFalse(true, "verifyPassword should throw an exception for non-existent user")
        } catch (e: NoSuchElementException) {
            assertTrue(true, "verifyPassword correctly throws exception for non-existent user")
        }
    }

    @Test
    fun testCreateAndGetUser() = runBlocking {
        val expiresAt = Clock.System.now().plus(1.hours)
        val user = userRepository.createUser(
            NewUser(
                name = "CreateAndGet User",
                email = "testCreateAndGetUser2@example.com",
                role = "CreateAndGet",
                expiresAt = expiresAt,
                password = "password"
            )
        )
        val actualUser =
            User(user.id, "CreateAndGet User", "testCreateAndGetUser2@example.com", "CreateAndGet", expiresAt)
        assertEquals(actualUser, user)

        val retrievedUser = userRepository.getUserById(user.id)
        assertEquals(actualUser, retrievedUser)

        val retrievedByEmail = userRepository.getUserByEmail("testCreateAndGetUser2@example.com")
        assertEquals(actualUser, retrievedByEmail)
    }

    @Test
    fun testUpdateUser() = runBlocking {
        val user = insertUser()
        val updatedUser = userRepository.updateUser(
            UpdateUser(
                id = user.id,
                name = "testUpdateUser User",
                email = "testUpdateUser@example.com",
                role = "ADMIN",
                password = null,
                expiresAt = user.expiresAt
            )
        )

        val actualUser = User(user.id, "testUpdateUser User", "testUpdateUser@example.com", "ADMIN", user.expiresAt)
        assertEquals(actualUser, updatedUser)
    }

    @Test
    fun `test update user password`() = runBlocking {
        val user = insertUser()
        assertTrue(userRepository.verifyPassword(user.name, "password"), "Initial password should be verified")

        userRepository.updateUser(UpdateUser(id = user.id, password = "newpassword"))

        assertFalse(userRepository.verifyPassword(user.name, "password"), "Old password should no longer work")
        assertTrue(userRepository.verifyPassword(user.name, "newpassword"), "New password should be verified")
    }

    @Test
    fun `test update user with null values`() = runBlocking {
        val user = insertUser()
        val updatedUser = userRepository.updateUser(UpdateUser(id = user.id, name = "Updated Name"))

        assertNotNull(updatedUser, "Updated user should not be null")
        assertEquals("Updated Name", updatedUser.name, "Name should be updated")
        assertEquals(user.email, updatedUser.email, "Email should remain unchanged")
        assertEquals(user.role, updatedUser.role, "Role should remain unchanged")
        assertEquals(user.expiresAt, updatedUser.expiresAt, "ExpiresAt should remain unchanged")

        assertTrue(userRepository.verifyPassword(updatedUser.name, "password"), "Password should remain unchanged")
    }

    @Test
    fun testDeleteUser() = runBlocking {
        val user = insertUser(Clock.System.now())
        assertTrue(userRepository.deleteUser(user.id))
        val retrievedUser = userRepository.getUserById(user.id)
        assertNull(retrievedUser)
    }

    @Test
    fun `test getAllUsers returns all users`() = runBlocking {
        val existingUsers = userRepository.getAllUsers()
        existingUsers.forEach { userRepository.deleteUser(it.id) }

        val user1 = insertUser()
        val user2 = insertUser()
        val user3 = insertUser()

        val allUsers = userRepository.getAllUsers()
        assertEquals(3, allUsers.size, "Should return all 3 inserted users")
        assertTrue(allUsers.any { it.id == user1.id }, "Should contain user1")
        assertTrue(allUsers.any { it.id == user2.id }, "Should contain user2")
        assertTrue(allUsers.any { it.id == user3.id }, "Should contain user3")
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `test getUserByEmail with non-existent email`() = runBlocking {
        val nonExistentEmail = "nonexistent${Uuid.random()}@example.com"
        val user = userRepository.getUserByEmail(nonExistentEmail)
        assertNull(user, "Should return null for non-existent email")
    }

    @Test
    fun `test deleteUser returns false for non-existent user`() = runBlocking {
        val nonExistentUserId = 999999 // Using a very large ID that is unlikely to exist
        val result = userRepository.deleteUser(nonExistentUserId)
        assertFalse(result, "Deleting a non-existent user should return false")
    }

    private suspend fun insertUser(instant: Instant = Clock.System.now().plus(30.days)): User =
        userRepository.createUser(newTestUser(instant))
}

@OptIn(ExperimentalUuidApi::class)
fun newTestUser(instant: Instant = Clock.System.now().plus(30.days)): NewUser {
    val random = Uuid.random()
    return NewUser(
        name = "$random User",
        email = "$random@example.com",
        role = "USER",
        password = "password",
        expiresAt = instant
    )
}
