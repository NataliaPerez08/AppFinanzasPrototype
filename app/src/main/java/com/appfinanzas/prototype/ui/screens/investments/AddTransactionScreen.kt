package com.appfinanzas.prototype.ui.screens.investments

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
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.ui.components.FinanceButton
import com.appfinanzas.prototype.ui.components.FinanceConfirmDialog
import com.appfinanzas.prototype.ui.components.FinanceErrorText
import com.appfinanzas.prototype.ui.components.FinanceOption
import com.appfinanzas.prototype.ui.components.FinanceOptionSelector
import com.appfinanzas.prototype.ui.components.FinancePanel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.FinanceTextField
import com.appfinanzas.prototype.ui.components.PrimaryMetric
import com.appfinanzas.prototype.ui.format.MoneyFormatter
import com.appfinanzas.prototype.ui.navigation.Routes

private val transactionTypeOptions = TransactionType.entries.map { type ->
    val label = when (type) {
        TransactionType.COMPRA -> "Compra"
        TransactionType.VENTA -> "Venta"
        TransactionType.DIVIDENDO -> "Dividendo"
        TransactionType.INTERES -> "Interés"
        TransactionType.DEPOSITO -> "Depósito"
        TransactionType.RETIRO -> "Retiro"
        TransactionType.COMISION -> "Comisión"
    }
    FinanceOption(value = type.name, label = label)
}

@Composable
fun AddTransactionScreen(
    investmentId: Long,
    onNavigate: (String) -> Unit,
    transactionId: Long? = null,
) {
    val viewModel: AddTransactionViewModel =
        viewModel(factory = AddTransactionViewModel.factory(investmentId, transactionId))
    val state by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.savedEvents.collect { savedId ->
            onNavigate(Routes.investmentDetail(savedId))
        }
    }

    FinanceScreen(
        title = if (state.isEditing) "Editar movimiento" else "Registrar movimiento",
        subtitle = state.investmentHeader?.subtitle.orEmpty(),
        selectedTab = Routes.INVESTMENTS,
        onNavigate = onNavigate,
    ) {
        FinanceOptionSelector(
            label = "Tipo",
            options = transactionTypeOptions,
            selectedValue = state.type?.name,
            onSelect = viewModel::onTypeSelected,
        )
        FinanceTextField(
            label = "Fecha",
            value = state.dateText,
            onValueChange = viewModel::onDateChange,
            error = state.fieldErrors["date"],
        )
        val needsQuantityAndPrice = state.type == TransactionType.COMPRA || state.type == TransactionType.VENTA
        if (needsQuantityAndPrice) {
            FinanceTextField(
                label = "Cantidad",
                value = state.quantityText,
                onValueChange = viewModel::onQuantityChange,
                error = state.fieldErrors["quantity"],
            )
            FinanceTextField(
                label = "Precio ${state.currency.code}",
                value = state.priceText,
                onValueChange = viewModel::onPriceChange,
                error = state.fieldErrors["price"],
            )
            FinanceTextField(
                label = "Comisión ${state.currency.code}",
                value = state.commissionText,
                onValueChange = viewModel::onCommissionChange,
                error = state.fieldErrors["commission"],
            )
        } else {
            FinanceTextField(
                label = "Monto ${state.currency.code}",
                value = state.quantityText,
                onValueChange = viewModel::onQuantityChange,
                error = state.fieldErrors["quantity"],
            )
        }
        val formError = state.formError
        if (formError != null) {
            FinanceErrorText(formError)
        }
        Spacer(Modifier.height(8.dp))
        FinancePanel {
            PrimaryMetric(
                label = "Total · ${state.currency.code}",
                value = MoneyFormatter.format(state.total, state.currency),
            )
        }
        Spacer(Modifier.height(8.dp))
        FinanceButton(
            text = if (state.isEditing) "Guardar cambios" else "Guardar movimiento",
            enabled = !state.isSubmitting,
            onClick = viewModel::submit,
        )
        if (state.isEditing) {
            Spacer(Modifier.height(8.dp))
            FinanceButton(
                text = "Eliminar movimiento",
                enabled = !state.isSubmitting,
                onClick = { showDeleteDialog = true },
            )
        }
    }

    if (showDeleteDialog) {
        FinanceConfirmDialog(
            title = "Eliminar movimiento",
            message = "Esta acción no se puede deshacer. El saldo de la inversión se recalculará.",
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}
