package com.appfinanzas.prototype.security

interface PinRepository {
    fun isEnabled(): Boolean
    fun hasPin(): Boolean
    fun setPin(pin: String): Boolean
    fun verifyPin(pin: String): Boolean
    fun clearPin()
    fun clearAll()

    fun isBiometricEnabled(): Boolean
    fun setBiometricEnabled(enabled: Boolean)

    fun autoLockMillis(): Long
    fun setAutoLockMillis(millis: Long)

    fun failedAttempts(): Int
    fun setFailedAttempts(count: Int)
    fun lastFailureAt(): Long
    fun setLastFailureAt(epochMillis: Long)
}
