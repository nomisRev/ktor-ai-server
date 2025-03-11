package org.jetbrains.ktor.sample.users

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom

class SaltAndHash(val salt: ByteArray, val hash: ByteArray) {
    operator fun component1() = salt
    operator fun component2() = hash
}

class Argon2Hasher(
    private val memory: Int = 65536,    // 64MB
    private val iterations: Int = 3,
    private val parallelism: Int = 4,
    private val outputLength: Int = 32,  // 256 bits
    private val secureRandom: SecureRandom = SecureRandom()
) {
    private fun generateSalt(): ByteArray =
        ByteArray(16).apply {
            secureRandom.nextBytes(this)
        }

    /**
     * Though Argon2 is CPU-intensive, [Dispatchers.IO] is still preferable over [Dispatchers.Default] because:
     *   - It provides a dedicated thread pool that can grow as needed within bounds
     *   - It prevents lengthy Argon2 operations from saturating the `Default` scheduler, which should be reserved for 'short' CPU tasks
     *   - It offers better isolation between Argon2 operations and other coroutines.
     *
     *   TODO: Consider using `private val argon = Dispatchers.IO.limitedParallelism(4)`
     */
    suspend fun encrypt(password: String): SaltAndHash = withContext(Dispatchers.IO) {
        val salt = generateSalt()
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withMemoryAsKB(memory)
            .withIterations(iterations)
            .withParallelism(parallelism)
            .build()

        val hash = ByteArray(outputLength)
        Argon2BytesGenerator().apply {
            init(builder)
        }.generateBytes(password.toCharArray(), hash)

        SaltAndHash(salt, hash)
    }

    suspend fun verify(password: String, salt: ByteArray, hash: ByteArray): Boolean = withContext(Dispatchers.IO) {
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withMemoryAsKB(memory)
            .withIterations(iterations)
            .withParallelism(parallelism)
            .build()

        val result = ByteArray(outputLength)
        Argon2BytesGenerator().apply {
            init(builder)
            generateBytes(password.toCharArray(), result)
        }

        result.contentEquals(hash)
    }
}
