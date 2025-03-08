package org.jetbrains.ktor.sample.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.ktor.sample.PostgresContainer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserServiceTest {

    private lateinit var database: Database
    private val userRepository by lazy { UserRepository(database) }

    @Before
    fun setUp() {
        val config = PostgresContainer.getDatabaseConfig()
        database = Database.connect(
            url = config.jdbcUrl,
            driver = config.driverClassName,
            user = config.username,
            password = config.password
        )

        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    @After
    fun tearDown() {
        TransactionManager.closeAndUnregister(database)
    }

    @Test
    fun testCreateAndGetUser() {
        val expiresAt = Clock.System.now().plus(3600.seconds)
        val user = userRepository.createUser(
            name = "Test User",
            email = "test@example.com",
            role = "USER",
            expiresAt = expiresAt
        )
        val actualUser = User(user.id, "Test User", "test@example.com", "USER", expiresAt)
        assertEquals(actualUser, user)

        val retrievedUser = userRepository.getUserById(user.id)
        assertEquals(actualUser, retrievedUser)

        val retrievedByEmail = userRepository.getUserByEmail("test@example.com")
        assertEquals(actualUser, retrievedByEmail)
    }

    @Test
    fun testUpdateUser() {
        val expiresAt = Clock.System.now().plus(3600.seconds)
        val user = insertUser(expiresAt)
        val updatedUser = userRepository.updateUser(
            id = user.id,
            name = "Updated User",
            email = "updated@example.com",
            role = "ADMIN",
            expiresAt = expiresAt
        )

        val actualUser = User(user.id, "Updated User", "updated@example.com", "ADMIN", expiresAt)
        assertEquals(actualUser, updatedUser)
    }

    @Test
    fun testDeleteUser() {
        val user = insertUser(Clock.System.now())
        assertTrue(userRepository.deleteUser(user.id))
        val retrievedUser = userRepository.getUserById(user.id)
        assertNull(retrievedUser)
    }

    private fun insertUser(instant: Instant): User =
        userRepository.createUser(
            name = "Test User",
            email = "test@example.com",
            role = "USER",
            expiresAt = instant
        )
}
