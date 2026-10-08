package com.appfinanzas.prototype.ui.screens.projection

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.ui.components.AssetRow
import com.appfinanzas.prototype.ui.components.FinanceEmptyState
import com.appfinanzas.prototype.ui.components.FinanceErrorState
import com.appfinanzas.prototype.ui.components.FinanceLabel
import com.appfinanzas.prototype.ui.components.FinanceLoadingState
import com.appfinanzas.prototype.ui.components.FinancePanel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.FinanceTextField
import com.appfinanzas.prototype.ui.components.MoneyMetric
import com.appfinanzas.prototype.ui.components.PrimaryMetric
import com.appfinanzas.prototype.ui.components.ProjectionChart
import com.appfinanzas.prototype.ui.components.SectionHeader
import com.appfinanzas.prototype.ui.components.SegmentedFilter
import com.appfinanzas.prototype.ui.format.MoneyFormatter
import com.appfinanzas.prototype.ui.navigation.Routes
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun ProjectionScreen(onNavigate: (String) -> Unit) {
    val viewModel: ProjectionViewModel = viewModel(factory = ProjectionViewModel.Factory)
    val state by viewModel.uiState.collectAsState()
    FinanceScreen(
        title = "Proyección · ${horizonLabel(state.horizonMonths)}",
        subtitle = "Escenarios para tu patrimonio",
        selectedTab = Routes.PROJECTION,
        onNavigate = onNavigate,
    ) {
        when {
            state.isLoading -> FinanceLoadingState()
            state.error != null -> FinanceErrorState(state.error!!, onRetry = viewModel::retry)
            state.isEmpty -> FinanceEmptyState(
                title = "SIN PATRIMONIO",
                message = "Registra tus inversiones\npara proyectar tu patrimonio.",
                actionLabel = "+ Agregar inversión",
                onAction = { onNavigate(Routes.ADD_INVESTMENT) },
            )
            else -> ProjectionContent(state, viewModel)
        }
    }
}

@Composable
private fun ProjectionContent(
    state: ProjectionUiState,
    viewModel: ProjectionViewModel,
) {
    SegmentedFilter(
        options = projectionHorizons.map { horizonLabel(it) },
        selected = horizonLabel(state.horizonMonths),
        onSelect = { label ->
            projectionHorizons.firstOrNull { horizonLabel(it) == label }?.let(viewModel::onHorizonSelected)
        },
    )
    Spacer(Modifier.height(8.dp))
    FinancePanel {
        PrimaryMetric(
            label = if (state.p50 != null) "P50" else "Proyectado",
            value = MoneyFormatter.format(state.nominal, state.baseCurrency),
            accent = true,
        )
        MoneyMetric(label = "Valor actual", amount = state.currentValue, currency = state.baseCurrency)
        MoneyMetric(label = "Ganancia esperada", amount = state.expectedGain, currency = state.baseCurrency, accent = true)
    }

    val p10 = state.p10
    val p90 = state.p90
    if (p10 != null && p90 != null) {
        Spacer(Modifier.height(8.dp))
        FinancePanel {
            SectionHeader(title = "Rango probable")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MoneyMetric(label = "P10", amount = p10, currency = state.baseCurrency)
                MoneyMetric(label = "P90", amount = p90, currency = state.baseCurrency)
            }
        }
    }

    val band = state.chartBand
    if (band != null) {
        Spacer(Modifier.height(8.dp))
        FinancePanel {
            SectionHeader(title = "Proyección")
            ProjectionChart(
                band = band,
                contentDescription = "Proyección de ${state.nominal} ${state.baseCurrency.code} " +
                    "en ${horizonLabel(state.horizonMonths)}",
            )
        }
    }

    Spacer(Modifier.height(8.dp))
    FinancePanel {
        SectionHeader(title = "Fuentes de crecimiento")
        MoneyMetric(label = "Contribuciones", amount = state.contributions, currency = state.baseCurrency)
        MoneyMetric(
            label = "Rendimiento de inversiones",
            amount = state.investmentReturns,
            currency = state.baseCurrency,
            accent = true,
        )
        MoneyMetric(label = "ISR estimado", amount = state.estimatedTaxes, currency = state.baseCurrency)
        MoneyMetric(label = "Valor real", amount = state.real, currency = state.baseCurrency)
    }

    if (state.scenarios.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        FinancePanel {
            SectionHeader(title = "Escenarios")
            state.scenarios.forEach { scenario ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = scenario.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = FinanzasColors.Text,
                        )
                        FinanceLabel("REAL ${MoneyFormatter.format(scenario.real, state.baseCurrency)}")
                    }
                    Text(
                        text = MoneyFormatter.format(scenario.nominal, state.baseCurrency),
                        style = MaterialTheme.typography.titleSmall,
                        color = FinanzasColors.Text,
                    )
                }
            }
        }
    }

    if (state.institutions.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        FinancePanel {
            SectionHeader(title = "Por institución")
            var expanded by remember { mutableStateOf(emptySet<String>()) }
            state.institutions.forEach { institution ->
                InstitutionProjectionRow(
                    institution = institution,
                    baseCurrency = state.baseCurrency,
                    expanded = institution.name in expanded,
                    onToggle = {
                        expanded = if (institution.name in expanded) {
                            expanded - institution.name
                        } else {
                            expanded + institution.name
                        }
                    },
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    FinancePanel {
        SectionHeader(title = "Supuestos")
        FinanceTextField(
            label = "Rendimiento esperado anual",
            value = state.expectedReturnText,
            onValueChange = viewModel::onExpectedReturnChange,
        )
        FinanceTextField(
            label = "Volatilidad estimada anual",
            value = state.volatilityText,
            onValueChange = viewModel::onVolatilityChange,
        )
        FinanceTextField(
            label = "Inflación estimada anual",
            value = state.inflationText,
            onValueChange = viewModel::onInflationChange,
        )
        FinanceTextField(
            label = "ISR estimado",
            value = state.isrText,
            onValueChange = viewModel::onIsrChange,
        )
    }
}

@Composable
private fun InstitutionProjectionRow(
    institution: ProjectionInstitutionUi,
    baseCurrency: Currency,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FinanzasColors.Divider)
                .clickable(onClick = onToggle)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = institution.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = FinanzasColors.Text,
                )
                FinanceLabel(if (expanded) "OCULTAR ACTIVOS" else "VER ACTIVOS")
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = MoneyFormatter.format(institution.projectedValue, baseCurrency),
                    style = MaterialTheme.typography.titleSmall,
                    color = FinanzasColors.Text,
                )
                Text(
                    text = MoneyFormatter.format(institution.expectedGain, baseCurrency),
                    style = MaterialTheme.typography.labelMedium,
                    color = FinanzasColors.Accent,
                )
            }
        }
        if (expanded) {
            institution.assets.forEach { asset ->
                AssetRow(
                    name = asset.label,
                    subtitle = asset.strategyLabel,
                    value = MoneyFormatter.format(asset.projectedValue, baseCurrency),
                    change = MoneyFormatter.format(asset.expectedGain, baseCurrency),
                    onClick = {},
                )
            }
        }
    }
}
