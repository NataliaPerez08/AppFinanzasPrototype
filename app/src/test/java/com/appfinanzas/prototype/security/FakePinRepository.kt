package com.appfinanzas.prototype.security

class FakePinRepository : PinRepository {

    private var credential: PinCredential? = null
    private var enabled = false
    private var biometric = false
    private var autoLock = LockPolicy.DEFAULT_AUTO_LOCK_MILLIS
    private var attempts = 0
    private var lastFailure = 0L

    override fun isEnabled(): Boolean = enabled && hasPin()

    override fun hasPin(): Boolean = credential != null

    override fun setPin(pin: String): Boolean {
        if (!PinHasher.isValidPin(pin)) return false
        credential = PinHasher.hash(pin, PinHasher.newSalt(), iterations = TEST_ITERATIONS)
        enabled = true
        attempts = 0
        lastFailure = 0L
        return true
    }

    override fun verifyPin(pin: String): Boolean {
        val stored = credential ?: return false
        return PinHasher.verify(pin, stored)
    }

    override fun clearPin() {
        credential = null
        enabled = false
        attempts = 0
        lastFailure = 0L
    }

    override fun clearAll() {
        credential = null
        enabled = false
        biometric = false
        autoLock = LockPolicy.DEFAULT_AUTO_LOCK_MILLIS
        attempts = 0
        lastFailure = 0L
    }

    override fun isBiometricEnabled(): Boolean = biometric

    override fun setBiometricEnabled(enabled: Boolean) {
        biometric = enabled
    }

    override fun autoLockMillis(): Long = autoLock

    override fun setAutoLockMillis(millis: Long) {
        autoLock = millis
    }

    override fun failedAttempts(): Int = attempts

    override fun setFailedAttempts(count: Int) {
        attempts = count
    }

    override fun lastFailureAt(): Long = lastFailure

    override fun setLastFailureAt(epochMillis: Long) {
        lastFailure = epochMillis
    }

    companion object {
        const val TEST_ITERATIONS = 1_000
    }
}
