package com.appfinanzas.prototype.ui.screens.more

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.components.FinanceButton
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.InstitutionRow
import com.appfinanzas.prototype.ui.navigation.Routes

@Composable
fun InstitutionsScreen(onNavigate: (String) -> Unit) {
    FinanceScreen(
        title = "Instituciones",
        subtitle = "Administra tus instituciones",
        selectedTab = Routes.MORE,
        onNavigate = onNavigate,
    ) {
        InstitutionRow(name = "GBM", detail = "Casa de Bolsa", status = "ACTIVA")
        Spacer(Modifier.height(4.dp))
        InstitutionRow(name = "CETES Directo", detail = "Renta fija", status = "ACTIVA")
        Spacer(Modifier.height(4.dp))
        InstitutionRow(name = "NU", detail = "SOFIPO", status = "ACTIVA")
        Spacer(Modifier.height(4.dp))
        InstitutionRow(name = "BBVA", detail = "Banco", status = "INACTIVA")
        Spacer(Modifier.height(8.dp))
        FinanceButton(text = "+ Agregar institución") {}
    }
}