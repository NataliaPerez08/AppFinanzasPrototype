package com.appfinanzas.prototype.ui.screens.more

import androidx.compose.runtime.Composable
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.FinanceTextField
import com.appfinanzas.prototype.ui.navigation.Routes

@Composable
fun SettingsScreen(onNavigate: (String) -> Unit) {
    FinanceScreen(
        title = "Configuración",
        subtitle = "Parámetros generales",
        selectedTab = Routes.MORE,
        onNavigate = onNavigate,
    ) {
        FinanceTextField(label = "Moneda base", value = "MXN", onValueChange = {})
        FinanceTextField(label = "Inflación estimada", value = "4.0 %", onValueChange = {})
        FinanceTextField(label = "ISR estimado", value = "2.0 %", onValueChange = {})
    }
}