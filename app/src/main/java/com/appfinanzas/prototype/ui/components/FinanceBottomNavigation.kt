package com.appfinanzas.prototype.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.navigation.Routes
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun FinanceBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        Routes.DASHBOARD to "INICIO",
        Routes.INVESTMENTS to "INVERSIONES",
        Routes.PORTFOLIO to "PATRIMONIO",
        Routes.PROJECTION to "PROYECCIÓN",
        Routes.MORE to "MÁS",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FinanzasColors.Surface)
            .border(1.dp, FinanzasColors.Divider)
            .navigationBarsPadding()
            .height(62.dp),
    ) {
        tabs.forEach { (route, label) ->
            val selected = currentRoute == route
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onNavigate(route) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) FinanzasColors.Accent else FinanzasColors.Text,
                )
            }
        }
    }
}