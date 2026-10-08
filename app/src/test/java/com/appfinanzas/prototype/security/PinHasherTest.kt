package com.appfinanzas.prototype.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {

    private val iterations = 1_000

    @Test
    fun `same pin and salt derive the same credential`() {
        val salt = PinHasher.newSalt()
        val first = PinHasher.hash("1234", salt, iterations)
        val second = PinHasher.hash("1234", salt, iterations)

        assertArrayEquals(first.hash, second.hash)
    }

    @Test
    fun `different salt derives a different credential`() {
        val first = PinHasher.hash("1234", PinHasher.newSalt(), iterations)
        val second = PinHasher.hash("1234", PinHasher.newSalt(), iterations)

        assertFalse(first.hash.contentEquals(second.hash))
    }

    @Test
    fun `correct pin verifies and wrong pin fails`() {
        val credential = PinHasher.hash("1234", PinHasher.newSalt(), iterations)

        assertTrue(PinHasher.verify("1234", credential))
        assertFalse(PinHasher.verify("4321", credential))
    }

    @Test
    fun `credential stores versioned algorithm parameters`() {
        val credential = PinHasher.hash("1234", PinHasher.newSalt(), iterations)

        assertEquals(PinHasher.ALGORITHM_VERSION, credential.algorithmVersion)
        assertEquals(iterations, credential.iterations)
        assertEquals(PinHasher.KEY_LENGTH, credential.keyLength)
        assertEquals(16, credential.salt.size)
    }

    @Test
    fun `unknown algorithm version is rejected`() {
        val credential = PinHasher.hash("1234", PinHasher.newSalt(), iterations).copy(algorithmVersion = 99)

        assertFalse(PinHasher.verify("1234", credential))
    }

    @Test
    fun `pin must be exactly four digits`() {
        assertTrue(PinHasher.isValidPin("0123"))
        assertFalse(PinHasher.isValidPin("123"))
        assertFalse(PinHasher.isValidPin("12a4"))
        assertFalse(PinHasher.isValidPin("12345"))
    }
}
