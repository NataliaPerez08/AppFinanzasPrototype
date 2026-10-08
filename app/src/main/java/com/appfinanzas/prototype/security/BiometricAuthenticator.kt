package com.appfinanzas.prototype.security

import android.content.Context
import androidx.biometric.BiometricManager

object BiometricAuthenticator {

    fun canAuthenticate(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK,
        ) == BiometricManager.BIOMETRIC_SUCCESS
}
