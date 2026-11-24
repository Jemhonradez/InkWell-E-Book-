package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    data class HashedPassword(
        val saltHex: String,
        val hashHex: String
    )

    suspend fun hashPassword(password: String): HashedPassword = withContext(Dispatchers.Default) {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        val hash = pbkdf2(password.toCharArray(), salt)
        HashedPassword(
            saltHex = salt.toHex(),
            hashHex = hash.toHex()
        )
    }

    suspend fun verifyPassword(
        password: String,
        saltHex: String,
        expectedHashHex: String
    ): Boolean = withContext(Dispatchers.Default) {
        try {
            val salt = hexToBytes(saltHex)
            val expectedHash = hexToBytes(expectedHashHex)
            val candidateHash = pbkdf2(password.toCharArray(), salt)
            MessageDigest.isEqual(candidateHash, expectedHash)
        } catch (_: Exception) {
            false
        }
    }

    private fun pbkdf2(passwordChars: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(passwordChars, salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun hexToBytes(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Invalid hex string" }
        return ByteArray(hex.length / 2) { index ->
            hex.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
