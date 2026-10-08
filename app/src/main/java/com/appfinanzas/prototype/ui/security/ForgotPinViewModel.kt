package com.appfinanzas.prototype.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfinanzas.prototype.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPinViewModel : ViewModel() {

    private val _done = MutableStateFlow(false)
    val done = _done.asStateFlow()

    fun resetAllData() {
        viewModelScope.launch {
            AppContainer.resetAllData()
            _done.value = true
        }
    }
}
