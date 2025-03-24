package org.jetbrains.ktor.sample.users

import io.ktor.server.application.ApplicationEnvironment
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom

class SaltAndHash(val salt: ByteArray, val hash: ByteArray) {
    operator fun component1() = salt
    operator fun component2() = hash
}

data class Argon2HasherConfig(
    val memory: Int,
    val iterations: Int,
    val parallelism: Int,
    val outputLength: Int,
    val limitedParallelism: Int
) {
    companion object {
        fun load(environment: ApplicationEnvironment): Argon2HasherConfig = with(environment.config) {
            Argon2HasherConfig(
                memory = propertyOrNull("memory")?.getString()?.toInt() ?: 65536,
                iterations = propertyOrNull("iterations")?.getString()?.toInt() ?: 3,
                parallelism = propertyOrNull("parallelism")?.getString()?.toInt() ?: 4,
                outputLength = propertyOrNull("outputLength")?.getString()?.toInt() ?: 32,
                limitedParallelism = propertyOrNull("limitedParallelism")?.getString()?.toInt() ?: 4
            )
        }
    }
}

class Argon2Hasher(
    private val config: Argon2HasherConfig,
    private val secureRandom: SecureRandom = SecureRandom()
) {
    private val boundedIOContext =
        Dispatchers.IO.limitedParallelism(config.limitedParallelism) + CoroutineName("argon2-coroutine")

    private fun generateSalt(): ByteArray =
        ByteArray(16).apply {
            secureRandom.nextBytes(this)
        }

    suspend fun encrypt(password: Password): SaltAndHash = withContext(boundedIOContext) {
        val salt = generateSalt()
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withMemoryAsKB(config.memory)
            .withIterations(config.iterations)
            .withParallelism(config.parallelism)
            .build()

        val hash = ByteArray(config.outputLength)
        Argon2BytesGenerator().apply {
            init(builder)
        }.generateBytes(password.value.toCharArray(), hash)

        SaltAndHash(salt, hash)
    }

    suspend fun verify(password: String, salt: ByteArray, hash: ByteArray): Boolean = withContext(boundedIOContext) {
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withMemoryAsKB(config.memory)
            .withIterations(config.iterations)
            .withParallelism(config.parallelism)
            .build()

        val result = ByteArray(config.outputLength)
        Argon2BytesGenerator().apply {
            init(builder)
            generateBytes(password.toCharArray(), result)
        }

        result.contentEquals(hash)
    }
}
