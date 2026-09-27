package com.appfinanzas.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.ui.navigation.FinanzasApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.initialize(applicationContext)
        setContent { FinanzasApp() }
    }
}