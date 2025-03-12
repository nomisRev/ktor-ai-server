package org.jetbrains.ktor.sample.users

import kotlinx.coroutines.runBlocking
import org.jetbrains.ktor.sample.DatabaseSpec
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UserRepositoryTest : DatabaseSpec() {
    private val userRepository by lazy { UserRepository(database) }

    private suspend fun insertUser(): User =
        userRepository.createUser(newTestUser())

    @Test
    fun `test verifyPassword with correct password`() = runBlocking<Unit> {
        val user = insertUser()
        val result = userRepository.verifyPassword(user.name, "password")
        assertEquals(VerifyResult(true, user.id), result)
    }

    @Test
    fun `test verifyPassword with incorrect password`() = runBlocking {
        val user = insertUser()
        val result = userRepository.verifyPassword(user.name, "wrongpassword")
        assertEquals(VerifyResult(false, user.id), result)
    }

    @Test
    fun `test verifyPassword with non-existent user`() = runBlocking<Unit> {
        assertThrows<NoSuchElementException> {
            userRepository.verifyPassword("nonexistinguser", "password")
        }
    }

    @Test
    fun `create user`() = runBlocking {
        val user = userRepository.createUser(
            NewUser(
                name = "Create User",
                email = "CreateUser@example.com",
                role = "CreateRole",
                password = "password"
            )
        )
        assertAll(
            { assertEquals("Create User", user.name) },
            { assertEquals("CreateUser@example.com", user.email) },
            { assertEquals("CreateRole", user.role) }
        )
    }

    @Test
    fun `get user by id`() = runBlocking {
        val user = userRepository.createUser(
            NewUser(
                name = "Get User By Id",
                email = "GetUserById@example.com",
                role = "GetUserByIdRole",
                password = "password"
            )
        )

        val retrievedUser = userRepository.getUserById(user.id)
        assertAll(
            { assertEquals(retrievedUser?.name, user.name) },
            { assertEquals(retrievedUser?.email, user.email) },
            { assertEquals(retrievedUser?.role, user.role) }
        )
    }

    @Test
    fun testUpdateUser() = runBlocking {
        val user = insertUser()
        val updatedUser = userRepository.updateUser(
            user.id,
            UpdateUser(
                name = "testUpdateUser User",
                email = "testUpdateUser@example.com",
                role = "ADMIN"
            )
        )

        val actualUser = User(user.id, "testUpdateUser User", "testUpdateUser@example.com", "ADMIN", user.expiresAt)
        assertEquals(actualUser, updatedUser)
    }

    @Test
    fun `test update user password`() = runBlocking {
        val user = insertUser()
        val initial = userRepository.verifyPassword(user.name, "password")

        userRepository.updateUser(user.id, UpdateUser(password = "newpassword"))

        val failed = userRepository.verifyPassword(user.name, "password")
        val success = userRepository.verifyPassword(user.name, "newpassword")
        assertAll(
            { assertEquals(VerifyResult(true, user.id), initial, "Initial password should still be valid") },
            { assertEquals(VerifyResult(false, user.id), failed, "Old password should not be valid anymore") },
            { assertEquals(VerifyResult(true, user.id), success, "New password should be valid") }
        )
    }

    @Test
    fun `test update user with null values`() = runBlocking {
        val user = insertUser()
        val updatedUser = userRepository.updateUser(user.id, UpdateUser(name = "Updated Name"))
        val verify = userRepository.verifyPassword("Updated Name", "password")

        assertNotNull(updatedUser, "Updated user should not be null")
        assertAll(
            { assertEquals("Updated Name", updatedUser.name, "Name should be updated") },
            { assertEquals(user.email, updatedUser.email, "Email should remain unchanged") },
            { assertEquals(user.role, updatedUser.role, "Role should remain unchanged") },
            { assertEquals(user.expiresAt, updatedUser.expiresAt, "ExpiresAt should remain unchanged") },
            { assertEquals(VerifyResult(true, user.id), verify, "Password should remain unchanged") },
        )
    }

    @Test
    fun testDeleteUser() = runBlocking {
        val user = insertUser()
        val deleted = userRepository.deleteUser(user.id)
        val retrievedUser = userRepository.getUserById(user.id)
        assertAll(
            { assertTrue(deleted, "User should be deleted") },
            { assertNull(retrievedUser, "User should not be retrievable anymore") }
        )
    }

    @Test
    fun `test deleteUser returns false for non-existent user`() = runBlocking {
        val result = userRepository.deleteUser(Long.MIN_VALUE)
        assertFalse(result, "Deleting a non-existent user should return false")
    }
}
