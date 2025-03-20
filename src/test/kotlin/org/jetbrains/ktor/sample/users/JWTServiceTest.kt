package org.jetbrains.ktor.sample.users

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.JWTCredential
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.jetbrains.ktor.sample.AppTestConfig
import org.jetbrains.ktor.sample.DatabaseSpec
import org.jetbrains.ktor.sample.auth.JWTService
import org.junit.Test
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JWTServiceTest : DatabaseSpec() {
    private val jwtConfig by lazy { AppTestConfig.jwt }
    private val users by lazy { UserRepository(database, Argon2Hasher(AppTestConfig.argon2)) }
    private val jwtService by lazy { JWTService(AppTestConfig.jwt, users) }

    @Test
    fun `test generate token`() = runBlocking {
        val user = users.createUser(newTestUser())
        val token = jwtService.generateToken(user.id)

        val decodedJWT = JWT.decode(token)
        assertEquals(jwtConfig.issuer, decodedJWT.issuer)
        assertEquals(jwtConfig.audience, decodedJWT.audience.single())
        assertEquals(user.id, decodedJWT.getClaim("user_id").asLong())
        assertNotNull(decodedJWT.expiresAt)
        assertNotNull(decodedJWT.issuedAt)

        val updatedUser = users.getUserByIdOrNull(user.id)
        assertNotNull(updatedUser)
        assertTrue(updatedUser.expiresAt > user.expiresAt)
    }

    @Test
    fun `test validate token with valid token`() = runBlocking {
        val user = users.createUser(newTestUser())
        val token = jwtService.generateToken(user.id)

        val decodedJWT = JWT.decode(token)
        val jwtCredential = JWTCredential(decodedJWT)

        val userJWT = jwtService.validateToken(jwtCredential)

        assertNotNull(userJWT)
        assertEquals(user.id, userJWT.user.id)
        assertEquals(user.name, userJWT.user.name)
        assertEquals(user.email, userJWT.user.email)
        assertEquals(user.role, userJWT.user.role)
    }

    @Test
    fun `test validate token with missing user_id claim`() = runBlocking {
        val token = JWT.create()
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .withIssuedAt(Date())
            .sign(Algorithm.HMAC256(jwtConfig.secret))

        val decodedJWT = JWT.decode(token)
        val jwtCredential = JWTCredential(decodedJWT)

        val userJWT = jwtService.validateToken(jwtCredential)

        assertNull(userJWT)
    }

    @Test
    fun `test validate token with expired token`() = runBlocking {
        val user = users.createUser(newTestUser())
        val token = JWT.create()
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withClaim("user_id", user.id)
            .withExpiresAt(Date(System.currentTimeMillis() - 1000)) // Expired
            .withIssuedAt(Date(System.currentTimeMillis() - 3600000))
            .sign(Algorithm.HMAC256(jwtConfig.secret))

        val decodedJWT = JWT.decode(token)
        val jwtCredential = JWTCredential(decodedJWT)

        val userJWT = jwtService.validateToken(jwtCredential)

        assertNull(userJWT)
    }

    @Test
    fun `test validate token with non-existent user`() = runBlocking {
        val nonExistentUserId = 999999L
        val token = JWT.create()
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withClaim("user_id", nonExistentUserId)
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .withIssuedAt(Date())
            .sign(Algorithm.HMAC256(jwtConfig.secret))

        val decodedJWT = JWT.decode(token)
        val jwtCredential = JWTCredential(decodedJWT)

        val userJWT = jwtService.validateToken(jwtCredential)

        assertNull(userJWT)
    }

    @Test
    fun `test validate token with expired user token`() = runBlocking {
        val user = users.createUser(newTestUser())
        users.updateExpiresAt(user.id, Instant.fromEpochMilliseconds(0))

        val token = jwtService.generateToken(user.id)

        users.updateExpiresAt(user.id, Instant.fromEpochMilliseconds(0))

        val decodedJWT = JWT.decode(token)
        val jwtCredential = JWTCredential(decodedJWT)

        val userJWT = jwtService.validateToken(jwtCredential)

        assertNull(userJWT)
    }
}
