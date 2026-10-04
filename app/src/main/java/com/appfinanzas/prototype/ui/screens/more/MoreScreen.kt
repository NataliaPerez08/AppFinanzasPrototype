package com.appfinanzas.prototype.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.components.FinancePanel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.navigation.Routes
import com.appfinanzas.prototype.ui.theme.FinanzasColors
import com.pulso.patrimonio.R

@Composable
fun MoreScreen(onNavigate: (String) -> Unit) {
    FinanceScreen(
        title = "Más",
        subtitle = "Administración y parámetros",
        selectedTab = Routes.MORE,
        onNavigate = onNavigate,
    ) {
        FinancePanel {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = FinanzasColors.Text,
            )
            Text(
                text = stringResource(R.string.app_descriptor),
                style = MaterialTheme.typography.labelMedium,
                color = FinanzasColors.Text.copy(alpha = 0.65f),
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Routes.INSTITUTIONS) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "INSTITUCIONES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = FinanzasColors.Text,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleMedium,
                    color = FinanzasColors.Accent,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Routes.SETTINGS) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "CONFIGURACIÓN",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = FinanzasColors.Text,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleMedium,
                    color = FinanzasColors.Accent,
                )
            }
        }
    }
}
