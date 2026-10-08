package com.appfinanzas.prototype.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class PinCredential(
    val algorithmVersion: Int,
    val salt: ByteArray,
    val iterations: Int,
    val keyLength: Int,
    val hash: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PinCredential) return false
        return algorithmVersion == other.algorithmVersion &&
            salt.contentEquals(other.salt) &&
            iterations == other.iterations &&
            keyLength == other.keyLength &&
            hash.contentEquals(other.hash)
    }

    override fun hashCode(): Int {
        var result = algorithmVersion
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + iterations
        result = 31 * result + keyLength
        result = 31 * result + hash.contentHashCode()
        return result
    }
}

object PinHasher {

    const val ALGORITHM_VERSION = 1
    const val DEFAULT_ITERATIONS = 120_000
    const val KEY_LENGTH = 256

    private const val SALT_BYTES = 16
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun isValidPin(pin: String): Boolean = pin.length == 4 && pin.all { it.isDigit() }

    fun newSalt(random: SecureRandom = SecureRandom()): ByteArray =
        ByteArray(SALT_BYTES).also { random.nextBytes(it) }

    fun derive(
        pin: CharArray,
        salt: ByteArray,
        iterations: Int = DEFAULT_ITERATIONS,
        keyLength: Int = KEY_LENGTH,
    ): ByteArray {
        val spec = PBEKeySpec(pin, salt, iterations, keyLength)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun hash(
        pin: String,
        salt: ByteArray,
        iterations: Int = DEFAULT_ITERATIONS,
        keyLength: Int = KEY_LENGTH,
    ): PinCredential = PinCredential(
        algorithmVersion = ALGORITHM_VERSION,
        salt = salt,
        iterations = iterations,
        keyLength = keyLength,
        hash = derive(pin.toCharArray(), salt, iterations, keyLength),
    )

    fun verify(pin: String, credential: PinCredential): Boolean {
        if (credential.algorithmVersion != ALGORITHM_VERSION) return false
        val candidate = derive(pin.toCharArray(), credential.salt, credential.iterations, credential.keyLength)
        return MessageDigest.isEqual(candidate, credential.hash)
    }
}
