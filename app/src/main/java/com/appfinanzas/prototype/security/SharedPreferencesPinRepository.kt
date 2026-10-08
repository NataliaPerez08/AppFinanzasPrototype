package com.appfinanzas.prototype.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

class SharedPreferencesPinRepository(
    context: Context,
    preferencesName: String = "pulso_security",
) : PinRepository {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    override fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false) && hasPin()

    override fun hasPin(): Boolean =
        prefs.contains(KEY_SALT) && prefs.contains(KEY_CREDENTIAL)

    override fun setPin(pin: String): Boolean {
        if (!PinHasher.isValidPin(pin)) return false
        val salt = PinHasher.newSalt()
        val credential = PinHasher.hash(pin, salt)
        prefs.edit()
            .putBoolean(KEY_ENABLED, true)
            .putInt(KEY_ALGORITHM_VERSION, credential.algorithmVersion)
            .putInt(KEY_ITERATIONS, credential.iterations)
            .putInt(KEY_KEY_LENGTH, credential.keyLength)
            .putString(KEY_SALT, encode(salt))
            .putString(KEY_CREDENTIAL, encode(credential.hash))
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LAST_FAILURE_AT, 0L)
            .apply()
        return true
    }

    override fun verifyPin(pin: String): Boolean = runCatching {
        val stored = storedCredential() ?: return false
        PinHasher.verify(pin, stored)
    }.getOrDefault(false)

    override fun clearPin() {
        prefs.edit()
            .remove(KEY_ENABLED)
            .remove(KEY_ALGORITHM_VERSION)
            .remove(KEY_ITERATIONS)
            .remove(KEY_KEY_LENGTH)
            .remove(KEY_SALT)
            .remove(KEY_CREDENTIAL)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LAST_FAILURE_AT, 0L)
            .apply()
    }

    override fun clearAll() {
        prefs.edit().clear().apply()
    }

    override fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC, false)

    override fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC, enabled).apply()
    }

    override fun autoLockMillis(): Long =
        prefs.getLong(KEY_AUTO_LOCK_MILLIS, LockPolicy.DEFAULT_AUTO_LOCK_MILLIS)

    override fun setAutoLockMillis(millis: Long) {
        prefs.edit().putLong(KEY_AUTO_LOCK_MILLIS, millis).apply()
    }

    override fun failedAttempts(): Int = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)

    override fun setFailedAttempts(count: Int) {
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, count).apply()
    }

    override fun lastFailureAt(): Long = prefs.getLong(KEY_LAST_FAILURE_AT, 0L)

    override fun setLastFailureAt(epochMillis: Long) {
        prefs.edit().putLong(KEY_LAST_FAILURE_AT, epochMillis).apply()
    }

    private fun storedCredential(): PinCredential? {
        val salt = prefs.getString(KEY_SALT, null)?.let(::decode) ?: return null
        val hash = prefs.getString(KEY_CREDENTIAL, null)?.let(::decode) ?: return null
        return PinCredential(
            algorithmVersion = prefs.getInt(KEY_ALGORITHM_VERSION, PinHasher.ALGORITHM_VERSION),
            salt = salt,
            iterations = prefs.getInt(KEY_ITERATIONS, PinHasher.DEFAULT_ITERATIONS),
            keyLength = prefs.getInt(KEY_KEY_LENGTH, PinHasher.KEY_LENGTH),
            hash = hash,
        )
    }

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

    private companion object {
        const val KEY_ENABLED = "app_lock_enabled"
        const val KEY_ALGORITHM_VERSION = "pin_algorithm_version"
        const val KEY_ITERATIONS = "pin_iterations"
        const val KEY_KEY_LENGTH = "pin_key_length"
        const val KEY_SALT = "pin_salt"
        const val KEY_CREDENTIAL = "pin_credential"
        const val KEY_BIOMETRIC = "biometric_enabled"
        const val KEY_AUTO_LOCK_MILLIS = "auto_lock_millis"
        const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        const val KEY_LAST_FAILURE_AT = "last_failure_at"
    }
}
