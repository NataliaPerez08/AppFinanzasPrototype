package com.appfinanzas.prototype.security

sealed interface LockState {
    data object Loading : LockState
    data object Disabled : LockState
    data object Locked : LockState
    data object Unlocked : LockState
}

sealed interface UnlockResult {
    data object Success : UnlockResult
    data object Incorrect : UnlockResult
    data class Cooldown(val remainingMillis: Long) : UnlockResult
}
