package org.jetbrains.ktor.sample.users

import kotlinx.coroutines.runBlocking
import org.jetbrains.ktor.sample.DatabaseSpec
import org.jetbrains.ktor.sample.security.Role
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserRepositoryTest : DatabaseSpec() {
    private val userRepository by lazy {
        val hasher = Argon2Hasher(
            Argon2HasherConfig(
                memory = 65536,
                iterations = 3,
                parallelism = 4,
                outputLength = 32,
                limitedParallelism = 4
            )
        )
        UserRepository(database, hasher)
    }

    private suspend fun insertUser(): User =
        userRepository.createUser(newTestUser())!!

    @Test
    fun `test verifyPassword with correct password`() = runBlocking<Unit> {
        val user = insertUser()
        val result = userRepository.verifyPassword(user.name, "password")
        assert(VerifyResult(true, user.id) == result)
    }

    @Test
    fun `test verifyPassword with incorrect password`() = runBlocking {
        val user = insertUser()
        val result = userRepository.verifyPassword(user.name, "wrongpassword")
        assert(VerifyResult(false, user.id) == result)
    }

    @Test
    fun `test verifyPassword with non-existent user`() = runBlocking {
        val result = userRepository.verifyPassword("nonexistinguser", "password")
        assert(null == result)
    }

    @Test
    fun `create user`() = runBlocking {
        val user = userRepository.createUser(
            NewUser(
                name = "Create User",
                email = "CreateUser@example.com",
                role = Role.USER,
                password = "password"
            )
        )
        assertNotNull(user, "User should be created successfully")
        assertAll(
            { assert("Create User" == user.name) },
            { assert("CreateUser@example.com" == user.email) },
            { assert(Role.USER == user.role) }
        )
    }

    @Test
    fun `get user by id`() = runBlocking {
        val user = userRepository.createUser(
            NewUser(
                name = "Get User By Id",
                email = "GetUserById@example.com",
                role = Role.USER,
                password = "password"
            )
        )
        assertNotNull(user, "User should be created successfully")

        val retrievedUser = userRepository.getUserByIdOrNull(user.id)
        assertNotNull(retrievedUser, "Retrieved user should not be null")
        assertAll(
            { assert(retrievedUser.name == user.name) },
            { assert(retrievedUser.email == user.email) },
            { assert(retrievedUser.role == user.role) }
        )
    }

    @Test
    fun testUpdateUser() = runBlocking {
        val user = insertUser()
        val updatedUser = userRepository.updateUserOrNull(
            user.id,
            UpdateUser(
                name = "testUpdateUser User",
                email = "testUpdateUser@example.com"
            )
        )

        val actualUser = user.copy(
            name = "testUpdateUser User",
            email = "testUpdateUser@example.com"
        )
        assert(actualUser == updatedUser)
    }

    @Test
    fun `test update user password`() = runBlocking {
        val user = insertUser()
        val initial = userRepository.verifyPassword(user.name, "password")

        userRepository.updateUserOrNull(user.id, UpdateUser(password = "newpassword"))

        val failed = userRepository.verifyPassword(user.name, "password")
        val success = userRepository.verifyPassword(user.name, "newpassword")
        assertAll(
            { assert(VerifyResult(true, user.id) == initial) { "Initial password should still be valid" } },
            { assert(VerifyResult(false, user.id) == failed) { "Old password should not be valid anymore" } },
            { assert(VerifyResult(true, user.id) == success) { "New password should be valid" } }
        )
    }

    @Test
    fun `test update user with null values`() = runBlocking {
        val user = insertUser()
        val updatedUser = userRepository.updateUserOrNull(user.id, UpdateUser(name = "Updated Name"))
        val verify = userRepository.verifyPassword("Updated Name", "password")

        assertNotNull(updatedUser, "Updated user should not be null")
        assertAll(
            { assert("Updated Name" == updatedUser.name) { "Name should be updated" } },
            { assert(user.email == updatedUser.email) { "Email should remain unchanged" } },
            { assert(user.role == updatedUser.role) { "Role should remain unchanged" } },
            { assert(user.expiresAt == updatedUser.expiresAt) { "ExpiresAt should remain unchanged" } },
            { assert(VerifyResult(true, user.id) == verify) { "Password should remain unchanged" } },
        )
    }

    @Test
    fun testDeleteUser() = runBlocking {
        val user = insertUser()
        val deleted = userRepository.deleteUser(user.id)
        val retrievedUser = userRepository.getUserByIdOrNull(user.id)
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

    @Test
    fun `test createUser returns null for existing user`() = runBlocking {
        // Create a user
        val newUser = NewUser(
            name = "Existing User",
            email = "existing@example.com",
            role = Role.USER,
            password = "password"
        )
        val user = userRepository.createUser(newUser)
        assertNotNull(user, "First user creation should succeed")

        // Try to create the same user again
        val duplicateUser = userRepository.createUser(newUser)
        assertNull(duplicateUser, "Creating a user with the same name and email should return null")

        // Try to create a user with the same name but different email
        val sameNameUser = userRepository.createUser(
            NewUser(
                name = "Existing User",
                email = "different@example.com",
                role = Role.USER,
                password = "password"
            )
        )
        assertNull(sameNameUser, "Creating a user with the same name should return null")

        // Try to create a user with the same email but different name
        val sameEmailUser = userRepository.createUser(
            NewUser(
                name = "Different User",
                email = "existing@example.com",
                role = Role.USER,
                password = "password"
            )
        )
        assertNull(sameEmailUser, "Creating a user with the same email should return null")
    }
}
