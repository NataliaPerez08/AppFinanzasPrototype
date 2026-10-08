package com.appfinanzas.prototype.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockPolicyTest {

    @Test
    fun `cooldown starts on the fifth failure`() {
        assertTrue((1..4).all { LockPolicy.cooldownFor(it) == 0L })
        assertEquals(30_000L, LockPolicy.cooldownFor(5))
        assertEquals(60_000L, LockPolicy.cooldownFor(6))
        assertEquals(300_000L, LockPolicy.cooldownFor(7))
        assertEquals(900_000L, LockPolicy.cooldownFor(8))
        assertEquals(900_000L, LockPolicy.cooldownFor(20))
    }

    @Test
    fun `remaining cooldown decreases with elapsed time`() {
        assertEquals(30_000L, LockPolicy.remainingCooldown(5, lastFailureAt = 1_000L, now = 1_000L))
        assertEquals(15_000L, LockPolicy.remainingCooldown(5, lastFailureAt = 1_000L, now = 16_000L))
        assertEquals(0L, LockPolicy.remainingCooldown(5, lastFailureAt = 1_000L, now = 40_000L))
        assertEquals(0L, LockPolicy.remainingCooldown(3, lastFailureAt = 1_000L, now = 2_000L))
    }

    @Test
    fun `auto lock options include immediate and one minute default`() {
        assertEquals(5, LockPolicy.AUTO_LOCK_OPTIONS.size)
        assertEquals(0L, LockPolicy.AUTO_LOCK_OPTIONS.first().millis)
        assertEquals(60_000L, LockPolicy.DEFAULT_AUTO_LOCK_MILLIS)
    }

    @Test
    fun `should lock only once the timeout elapses`() {
        assertFalse(LockPolicy.shouldLock(59_999L, 60_000L))
        assertTrue(LockPolicy.shouldLock(60_000L, 60_000L))
        assertTrue(LockPolicy.shouldLock(1L, 0L))
    }
}
