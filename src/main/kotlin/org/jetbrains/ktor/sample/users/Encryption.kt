package org.jetbrains.ktor.sample.users

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom

class SaltAndHash(val salt: ByteArray, val hash: ByteArray) {
    operator fun component1() = salt
    operator fun component2() = hash
}

class Encryption(
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

    fun encrypt(password: String): SaltAndHash {
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

        return SaltAndHash(salt, hash)
    }

    fun verify(password: String, salt: ByteArray, hash: ByteArray): Boolean {
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

        return result.contentEquals(hash)
    }
}
