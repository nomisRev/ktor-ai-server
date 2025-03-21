package org.jetbrains.ktor.sample.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.JWTCredential
import kotlinx.datetime.Instant
import org.jetbrains.ktor.sample.users.Token
import org.jetbrains.ktor.sample.users.User
import org.jetbrains.ktor.sample.users.UserRepository
import java.time.Duration
import java.util.Date

data class UserJWT(val user: User)

class JWTService(private val config: JWTConfig, private val repository: UserRepository) {
    private val algorithm = Algorithm.HMAC256(config.secret)
    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withAudience(config.audience)
        .withIssuer(config.issuer)
        .build()

    private val ONE_HOUR = Duration.ofHours(1).toMillis()

    fun generateToken(userId: Long): Token {
        val issuedAt = System.currentTimeMillis()
        val expiresAt = issuedAt + ONE_HOUR

        repository.updateExpiresAt(userId, Instant.Companion.fromEpochMilliseconds(expiresAt))

        return Token(
            JWT.create()
                .withAudience(config.audience)
                .withIssuer(config.issuer)
                .withClaim("user_id", userId)
                .withExpiresAt(Date(expiresAt))
                .withIssuedAt(Date(issuedAt))
                .sign(algorithm)
        )
    }

    fun validateToken(credential: JWTCredential): UserJWT? {
        val userId = credential.getClaim("user_id", Long::class)
        val now = Date()

        return when {
            userId == null -> null

            credential.expiresAt?.before(now) == true -> null

            else -> {
                val user = repository.getUserByIdOrNull(userId)
                when {
                    user == null -> null
                    Date(user.expiresAt.toEpochMilliseconds()).before(now) -> null
                    else -> UserJWT(user)
                }
            }
        }
    }
}