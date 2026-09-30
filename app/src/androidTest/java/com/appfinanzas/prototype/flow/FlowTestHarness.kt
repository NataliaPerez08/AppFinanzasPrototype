package com.appfinanzas.prototype.flow

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.appfinanzas.prototype.data.local.AppDatabase
import com.appfinanzas.prototype.data.room.RoomInvestmentRepository
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.usecase.AddInvestmentResult
import com.appfinanzas.prototype.domain.usecase.AddInvestmentUseCase
import com.appfinanzas.prototype.domain.validation.InvestmentForm
import com.appfinanzas.prototype.domain.validation.TransactionForm
import java.time.LocalDate

class FlowTestHarness {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    val database: AppDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()

    val repository = RoomInvestmentRepository(database)

    suspend fun seedInstitution(name: String = "GBM"): Long =
        repository.addInstitution(Institution(0, name, "Casa de Bolsa"))

    suspend fun seedInvestment(
        institutionId: Long,
        initialValue: String = "10000",
        name: String = "Apple",
        symbol: String = "AAPL",
    ): Long {
        val result = AddInvestmentUseCase(repository)
            .execute(investmentForm(institutionId, initialValue, name, symbol))
        return (result as AddInvestmentResult.Success).investmentId
    }

    fun close() = database.close()

    companion object {
        fun pastDate(daysAgo: Long): String {
            val date = LocalDate.now().minusDays(daysAgo)
            return "%02d/%02d/%04d".format(date.dayOfMonth, date.monthValue, date.year)
        }

        fun investmentForm(
            institutionId: Long,
            initialValue: String = "10000",
            name: String = "Apple",
            symbol: String = "AAPL",
            currency: Currency = Currency.MXN,
        ) = InvestmentForm(
            type = InvestmentType.ACCION,
            institutionId = institutionId,
            name = name,
            symbol = symbol,
            currency = currency,
            initialValueText = initialValue,
            dateText = pastDate(2),
        )

        fun transactionForm(
            type: TransactionType,
            quantity: String = "",
            price: String = "",
            commission: String = "0",
        ) = TransactionForm(
            type = type,
            dateText = pastDate(1),
            quantityText = quantity,
            priceText = price,
            commissionText = commission,
            currency = Currency.MXN,
        )
    }
}
