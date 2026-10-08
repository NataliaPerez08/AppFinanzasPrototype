package com.appfinanzas.prototype.ui.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfinanzas.prototype.security.BiometricAuthenticator
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun UnlockScreen(
    onForgotPin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val biometricAvailable = remember { BiometricAuthenticator.canAuthenticate(context) }
    val viewModel: UnlockViewModel = viewModel(factory = UnlockViewModel.factory(biometricAvailable))
    val state by viewModel.uiState.collectAsState()
    val launchBiometric = rememberBiometricLauncher(onSuccess = viewModel::onBiometricSuccess)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinanzasColors.Background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))
            Text(
                text = "PULSO",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = FinanzasColors.Accent,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "DESBLOQUEAR PULSO",
                style = MaterialTheme.typography.labelMedium,
                color = FinanzasColors.Text,
            )
            Spacer(Modifier.height(28.dp))
            PinDots(filled = state.pinLength)
            Spacer(Modifier.height(12.dp))
            state.error?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelMedium,
                    color = FinanzasColors.Accent,
                )
            }
            if (state.cooldownRemainingMillis > 0L) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "ESPERA ${state.cooldownRemainingMillis / 1000}s",
                    style = MaterialTheme.typography.labelMedium,
                    color = FinanzasColors.Text.copy(alpha = 0.7f),
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PinKeypad(
                onDigit = viewModel::onDigit,
                onBackspace = viewModel::onBackspace,
                onBiometric = if (state.biometricAvailable && state.biometricEnabled) {
                    { launchBiometric() }
                } else {
                    null
                },
                enabled = state.cooldownRemainingMillis == 0L,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "OLVIDÉ MI PIN",
                style = MaterialTheme.typography.labelMedium,
                color = FinanzasColors.Accent,
                modifier = Modifier.clickable(onClick = onForgotPin),
            )
        }
    }
}

@Composable
private fun rememberBiometricLauncher(onSuccess: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentSuccess = rememberUpdatedState(onSuccess)
    return remember(context) {
        {
            val activity = context as? FragmentActivity
            if (activity != null) {
                val prompt = BiometricPrompt(
                    activity,
                    ContextCompat.getMainExecutor(activity),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult,
                        ) {
                            currentSuccess.value()
                        }
                    },
                )
                val info = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Desbloquear PULSO")
                    .setSubtitle("Usa tu biometría")
                    .setNegativeButtonText("Usar PIN")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                    .build()
                prompt.authenticate(info)
            }
        }
    }
}
