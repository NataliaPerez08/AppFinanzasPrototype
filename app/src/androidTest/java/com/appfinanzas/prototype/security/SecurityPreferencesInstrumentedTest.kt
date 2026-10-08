package com.appfinanzas.prototype.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityPreferencesInstrumentedTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefsName = "pulso_security_instrumented_test"

    private fun repository() = SharedPreferencesPinRepository(context, prefsName)

    @Test
    fun pinIsDerivedNotStoredAndResetsCleanly() {
        val repository = repository()
        repository.clearAll()
        assertFalse(repository.isEnabled())

        assertTrue(repository.setPin("1234"))
        assertTrue(repository.isEnabled())
        assertTrue(repository.verifyPin("1234"))
        assertFalse(repository.verifyPin("0000"))

        val storedValues = context.getSharedPreferences(prefsName, 0).all.values
        assertFalse("raw storage must not contain the plaintext PIN", storedValues.any { it == "1234" })

        repository.clearAll()
        assertFalse(repository.isEnabled())
        assertFalse(repository.hasPin())
    }

    @Test
    fun managerStartsLockedOnColdStartAndUnlocksWithPin() {
        val repository = repository()
        repository.clearAll()
        repository.setPin("4321")

        val manager = AppLockManager(repository)
        manager.initialize()
        assertEquals(LockState.Locked, manager.lockState.value)
        assertEquals(UnlockResult.Success, manager.submitPin("4321"))
        assertEquals(LockState.Unlocked, manager.lockState.value)

        manager.resetToDisabled()
        assertFalse(repository.isEnabled())
    }
}
