package com.appfinanzas.prototype.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppLockManager(
    private val repository: PinRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    private val _lockState = MutableStateFlow<LockState>(LockState.Loading)
    val lockState: StateFlow<LockState> = _lockState.asStateFlow()

    private var backgroundedAt: Long? = null

    fun initialize() {
        _lockState.value = if (repository.isEnabled()) LockState.Locked else LockState.Disabled
    }

    fun onForeground() {
        if (_lockState.value == LockState.Unlocked) {
            val leftAt = backgroundedAt
            if (leftAt != null && repository.isEnabled() &&
                LockPolicy.shouldLock(clock() - leftAt, repository.autoLockMillis())
            ) {
                _lockState.value = LockState.Locked
            }
        }
        backgroundedAt = null
    }

    fun onBackground() {
        if (_lockState.value == LockState.Unlocked) backgroundedAt = clock()
    }

    fun lock() {
        if (repository.isEnabled()) _lockState.value = LockState.Locked
    }

    fun submitPin(pin: String): UnlockResult {
        val cooldown = cooldownRemaining()
        if (cooldown > 0L) return UnlockResult.Cooldown(cooldown)
        return if (repository.verifyPin(pin)) {
            clearAttempts()
            _lockState.value = LockState.Unlocked
            UnlockResult.Success
        } else {
            registerFailure()
            UnlockResult.Incorrect
        }
    }

    fun onBiometricSuccess() {
        clearAttempts()
        _lockState.value = LockState.Unlocked
    }

    fun enable(newPin: String): Boolean {
        val ok = repository.setPin(newPin)
        if (ok) _lockState.value = LockState.Unlocked
        return ok
    }

    fun changePin(currentPin: String, newPin: String): UnlockResult {
        if (cooldownRemaining() > 0L) return UnlockResult.Cooldown(cooldownRemaining())
        if (!repository.verifyPin(currentPin)) return registerFailure()
        if (!PinHasher.isValidPin(newPin) || !repository.setPin(newPin)) return UnlockResult.Incorrect
        clearAttempts()
        return UnlockResult.Success
    }

    fun disable(currentPin: String): UnlockResult {
        if (cooldownRemaining() > 0L) return UnlockResult.Cooldown(cooldownRemaining())
        if (!repository.verifyPin(currentPin)) return registerFailure()
        repository.clearPin()
        repository.setBiometricEnabled(false)
        clearAttempts()
        _lockState.value = LockState.Disabled
        return UnlockResult.Success
    }

    fun cooldownRemaining(): Long =
        LockPolicy.remainingCooldown(repository.failedAttempts(), repository.lastFailureAt(), clock())

    fun isBiometricEnabled(): Boolean = repository.isBiometricEnabled()

    fun isEnabled(): Boolean = repository.isEnabled()

    fun verifyCurrentPin(pin: String): Boolean = repository.verifyPin(pin)

    fun setBiometricEnabled(enabled: Boolean) = repository.setBiometricEnabled(enabled)

    fun autoLockMillis(): Long = repository.autoLockMillis()

    fun setAutoLockMillis(millis: Long) = repository.setAutoLockMillis(millis)

    fun resetToDisabled() {
        repository.clearAll()
        _lockState.value = LockState.Disabled
    }

    private fun registerFailure(): UnlockResult.Incorrect {
        repository.setFailedAttempts(repository.failedAttempts() + 1)
        repository.setLastFailureAt(clock())
        return UnlockResult.Incorrect
    }

    private fun clearAttempts() {
        repository.setFailedAttempts(0)
        repository.setLastFailureAt(0L)
    }
}
