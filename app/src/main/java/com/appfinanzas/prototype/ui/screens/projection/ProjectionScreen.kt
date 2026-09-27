package com.appfinanzas.prototype.ui.screens.projection

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.components.FinancePanel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.FinanceTextField
import com.appfinanzas.prototype.ui.components.MoneyMetric
import com.appfinanzas.prototype.ui.components.PortfolioChart
import com.appfinanzas.prototype.ui.components.SectionHeader
import com.appfinanzas.prototype.ui.navigation.Routes

@Composable
fun ProjectionScreen(onNavigate: (String) -> Unit) {
    FinanceScreen(
        title = "Proyección a 1 año",
        subtitle = "Escenarios para tu patrimonio",
        selectedTab = Routes.PROJECTION,
        onNavigate = onNavigate,
    ) {
        FinanceTextField(
            label = "Rendimiento esperado anual",
            value = "8.5 %",
            onValueChange = {},
        )
        FinanceTextField(label = "Inflación estimada anual", value = "4.0 %", onValueChange = {})
        FinanceTextField(label = "ISR estimado", value = "2.0 %", onValueChange = {})
        Spacer(Modifier.height(8.dp))
        FinancePanel {
            SectionHeader(title = "Resultados estimados")
            MoneyMetric(label = "Valor nominal", amount = 1_351_172.0)
            MoneyMetric(label = "Después de ISR", amount = 1_324_148.0, accent = true)
            MoneyMetric(label = "Valor real", amount = 1_273_220.0)
        }
        Spacer(Modifier.height(8.dp))
        FinancePanel {
            SectionHeader(title = "Escenario base")
            PortfolioChart(
                points = listOf(.90f, .88f, .85f, .82f, .80f, .78f, .75f, .72f, .70f, .68f, .66f, .64f, .62f),
                contentDescription = "Proyección del patrimonio a un año",
            )
        }
    }
}