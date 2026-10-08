package com.appfinanzas.prototype.ui.security

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfinanzas.prototype.ui.components.FinanceButton
import com.appfinanzas.prototype.ui.components.FinanceLabel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.SectionHeader
import com.appfinanzas.prototype.ui.navigation.Routes

@Composable
fun ForgotPinScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onResetFinished: () -> Unit,
) {
    val viewModel: ForgotPinViewModel = viewModel()
    val done by viewModel.done.collectAsState()
    var step by remember { mutableStateOf(0) }

    LaunchedEffect(done) {
        if (done) onResetFinished()
    }

    FinanceScreen(
        title = "Olvidé mi PIN",
        subtitle = "No hay recuperación del PIN",
        selectedTab = Routes.MORE,
        onNavigate = onNavigate,
    ) {
        when (step) {
            0 -> {
                SectionHeader(title = "Sin recuperación de PIN")
                Spacer(Modifier.height(8.dp))
                FinanceLabel(
                    "PULSO no guarda tu PIN ni permite recuperarlo. " +
                        "La única salida es eliminar todos los datos locales.",
                )
                Spacer(Modifier.height(8.dp))
                FinanceLabel(
                    "Se borrarán inversiones, movimientos, instituciones, configuración y seguridad. " +
                        "Esta acción no se puede deshacer.",
                )
                Spacer(Modifier.height(16.dp))
                FinanceButton(text = "Entiendo, continuar") { step = 1 }
                Spacer(Modifier.height(8.dp))
                FinanceButton(text = "Cancelar", onClick = onBack)
            }
            else -> {
                SectionHeader(title = "Confirmación final")
                Spacer(Modifier.height(8.dp))
                FinanceLabel("¿Eliminar TODOS los datos de PULSO de forma permanente?")
                Spacer(Modifier.height(16.dp))
                FinanceButton(
                    text = "Eliminar todos los datos",
                    onClick = viewModel::resetAllData,
                )
                Spacer(Modifier.height(8.dp))
                FinanceButton(text = "Cancelar", onClick = onBack)
            }
        }
    }
}
