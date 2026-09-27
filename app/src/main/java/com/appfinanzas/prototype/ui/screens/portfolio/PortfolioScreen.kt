package com.appfinanzas.prototype.ui.screens.portfolio

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.components.AllocationBar
import com.appfinanzas.prototype.ui.components.AllocationItem
import com.appfinanzas.prototype.ui.components.FinancePanel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.MoneyMetric
import com.appfinanzas.prototype.ui.components.PercentageMetric
import com.appfinanzas.prototype.ui.components.SectionHeader
import com.appfinanzas.prototype.ui.navigation.Routes

@Composable
fun PortfolioScreen(onNavigate: (String) -> Unit) {
    FinanceScreen(
        title = "Patrimonio",
        subtitle = "Análisis y distribución de tu portafolio",
        selectedTab = Routes.PORTFOLIO,
        onNavigate = onNavigate,
    ) {
        FinancePanel {
            MoneyMetric(label = "Valor actual", amount = 1_245_320.0)
            MoneyMetric(label = "Capital aportado", amount = 1_050_000.0)
            MoneyMetric(label = "Ganancia", amount = 195_320.0, accent = true)
            PercentageMetric(label = "Rendimiento", percentage = 18.6)
        }
        Spacer(Modifier.height(8.dp))
        FinancePanel {
            SectionHeader(title = "Por tipo")
            AllocationBar(
                items = listOf(
                    AllocationItem(label = "Renta variable", percentage = 42f),
                    AllocationItem(label = "Renta fija", percentage = 38f),
                    AllocationItem(label = "SOFIPOs", percentage = 20f),
                ),
            )
            Spacer(Modifier.height(12.dp))
            SectionHeader(title = "Por institución")
            AllocationBar(
                items = listOf(
                    AllocationItem(label = "GBM", percentage = 45f),
                    AllocationItem(label = "CETES", percentage = 35f),
                    AllocationItem(label = "NU", percentage = 20f),
                ),
            )
        }
    }
}