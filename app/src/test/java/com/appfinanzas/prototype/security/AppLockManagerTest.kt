package com.appfinanzas.prototype.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppLockManagerTest {

    private lateinit var repo: FakePinRepository
    private var now = 1_000_000L

    @Before
    fun setUp() {
        repo = FakePinRepository()
        now = 1_000_000L
    }

    private fun manager() = AppLockManager(repo, clock = { now })

    @Test
    fun `disabled when no pin is configured`() {
        val manager = manager()
        manager.initialize()

        assertEquals(LockState.Disabled, manager.lockState.value)
    }

    @Test
    fun `cold start locks when app lock is enabled`() {
        repo.setPin("1234")
        val manager = manager()
        manager.initialize()

        assertEquals(LockState.Locked, manager.lockState.value)
    }

    @Test
    fun `correct pin unlocks and wrong pin stays locked`() {
        repo.setPin("1234")
        val manager = manager()
        manager.initialize()

        assertEquals(UnlockResult.Incorrect, manager.submitPin("0000"))
        assertEquals(LockState.Locked, manager.lockState.value)
        assertEquals(UnlockResult.Success, manager.submitPin("1234"))
        assertEquals(LockState.Unlocked, manager.lockState.value)
    }

    @Test
    fun `biometric success unlocks`() {
        repo.setPin("1234")
        val manager = manager()
        manager.initialize()

        manager.onBiometricSuccess()

        assertEquals(LockState.Unlocked, manager.lockState.value)
    }

    @Test
    fun `immediate auto lock relocks after leaving foreground`() {
        repo.setPin("1234")
        repo.setAutoLockMillis(0L)
        val manager = manager()
        manager.initialize()
        manager.submitPin("1234")

        manager.onBackground()
        now += 1
        manager.onForeground()

        assertEquals(LockState.Locked, manager.lockState.value)
    }

    @Test
    fun `timed auto lock waits until the timeout elapses`() {
        repo.setPin("1234")
        repo.setAutoLockMillis(60_000L)
        val manager = manager()
        manager.initialize()
        manager.submitPin("1234")

        manager.onBackground()
        now += 30_000
        manager.onForeground()
        assertEquals(LockState.Unlocked, manager.lockState.value)

        manager.onBackground()
        now += 60_000
        manager.onForeground()
        assertEquals(LockState.Locked, manager.lockState.value)
    }

    @Test
    fun `progressive cooldown blocks attempts then allows again`() {
        repo.setPin("1234")
        val manager = manager()
        manager.initialize()

        repeat(4) { manager.submitPin("0000") }
        assertEquals(0L, manager.cooldownRemaining())

        manager.submitPin("0000")
        assertTrue(manager.cooldownRemaining() > 0L)
        assertTrue(manager.submitPin("1234") is UnlockResult.Cooldown)

        now += 30_000
        assertEquals(UnlockResult.Success, manager.submitPin("1234"))
    }

    @Test
    fun `change pin requires the current pin`() {
        repo.setPin("1234")
        val manager = manager()
        manager.initialize()

        assertEquals(UnlockResult.Incorrect, manager.changePin("0000", "5678"))
        assertEquals(UnlockResult.Success, manager.changePin("1234", "5678"))
        assertTrue(repo.verifyPin("5678"))
        assertFalse(repo.verifyPin("1234"))
    }

    @Test
    fun `disable requires the current pin and clears biometric`() {
        repo.setPin("1234")
        repo.setBiometricEnabled(true)
        val manager = manager()
        manager.initialize()

        assertEquals(UnlockResult.Incorrect, manager.disable("0000"))
        assertEquals(UnlockResult.Success, manager.disable("1234"))
        assertEquals(LockState.Disabled, manager.lockState.value)
        assertFalse(repo.isBiometricEnabled())
    }

    @Test
    fun `reset clears credentials and disables the lock`() {
        repo.setPin("1234")
        val manager = manager()
        manager.initialize()

        manager.resetToDisabled()

        assertEquals(LockState.Disabled, manager.lockState.value)
        assertFalse(repo.isEnabled())
        assertFalse(repo.hasPin())
    }
}
