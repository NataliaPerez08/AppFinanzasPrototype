package com.appfinanzas.prototype.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.security.AppLockManager
import com.appfinanzas.prototype.security.UnlockResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UnlockUiState(
    val pinLength: Int = 0,
    val error: String? = null,
    val cooldownRemainingMillis: Long = 0L,
    val biometricAvailable: Boolean = false,
    val biometricEnabled: Boolean = false,
)

class UnlockViewModel(
    private val manager: AppLockManager,
    biometricAvailable: Boolean = false,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UnlockUiState(
            biometricAvailable = biometricAvailable,
            biometricEnabled = manager.isBiometricEnabled(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    private var pin = StringBuilder()
    private var cooldownJob: Job? = null

    init {
        startCooldownTicker()
    }

    fun onDigit(digit: Char) {
        if (_uiState.value.cooldownRemainingMillis > 0L || pin.length >= MAX_PIN) return
        pin.append(digit)
        _uiState.update { it.copy(pinLength = pin.length, error = null) }
        if (pin.length == MAX_PIN) submit()
    }

    fun onBackspace() {
        if (pin.isEmpty()) return
        pin.deleteCharAt(pin.length - 1)
        _uiState.update { it.copy(pinLength = pin.length, error = null) }
    }

    fun onBiometricSuccess() {
        manager.onBiometricSuccess()
    }

    private fun submit() {
        val entered = pin.toString()
        pin = StringBuilder()
        when (val result = manager.submitPin(entered)) {
            is UnlockResult.Success -> _uiState.update { it.copy(pinLength = 0, error = null) }
            is UnlockResult.Incorrect -> _uiState.update { it.copy(pinLength = 0, error = "PIN INCORRECTO") }
            is UnlockResult.Cooldown -> {
                _uiState.update {
                    it.copy(
                        pinLength = 0,
                        error = "DEMASIADOS INTENTOS",
                        cooldownRemainingMillis = result.remainingMillis,
                    )
                }
                startCooldownTicker()
            }
        }
    }

    private fun startCooldownTicker() {
        cooldownJob?.cancel()
        val initial = manager.cooldownRemaining()
        if (initial <= 0L) return
        _uiState.update { it.copy(cooldownRemainingMillis = initial) }
        cooldownJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val remaining = manager.cooldownRemaining()
                _uiState.update { it.copy(cooldownRemainingMillis = remaining) }
                if (remaining <= 0L) break
            }
        }
    }

    companion object {
        private const val MAX_PIN = 4

        fun factory(biometricAvailable: Boolean) = viewModelFactory {
            initializer {
                UnlockViewModel(AppContainer.appLockManager, biometricAvailable)
            }
        }
    }
}
