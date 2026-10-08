package com.appfinanzas.prototype

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.ui.navigation.FinanzasApp

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val surfaceScrim = android.graphics.Color.parseColor("#F4F3F1")
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(scrim = surfaceScrim, darkScrim = surfaceScrim),
            navigationBarStyle = SystemBarStyle.light(scrim = surfaceScrim, darkScrim = surfaceScrim),
        )

        AppContainer.initialize(applicationContext)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                AppContainer.appLockManager.onForeground()
            }

            override fun onStop(owner: LifecycleOwner) {
                AppContainer.appLockManager.onBackground()
            }
        })

        setContent { FinanzasApp() }
    }
}
