package com.appfinanzas.prototype.flow

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.appfinanzas.prototype.data.local.AppDatabase
import com.appfinanzas.prototype.data.room.RoomInvestmentRepository
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.usecase.AddInvestmentResult
import com.appfinanzas.prototype.domain.usecase.AddInvestmentUseCase
import com.appfinanzas.prototype.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenceFlowTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "flow-persistence-test.db"

    @Before
    fun deleteBefore() {
        context.deleteDatabase(databaseName)
    }

    @After
    fun deleteAfter() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun dataSurvivesDatabaseReopen() = runTest {
        var database = open(databaseName)
        var repository = RoomInvestmentRepository(database)
        val institutionId = repository.addInstitution(Institution(0, "GBM", "Casa de Bolsa"))
        val id = (AddInvestmentUseCase(repository).execute(FlowTestHarness.investmentForm(institutionId)) as AddInvestmentResult.Success).investmentId
        AddTransactionUseCase(repository).execute(id, FlowTestHarness.transactionForm(TransactionType.COMPRA, "2", "100"))
        database.close()

        database = open(databaseName)
        repository = RoomInvestmentRepository(database)

        val investment = repository.getInvestment(id)
        assertNotNull(investment)
        assertEquals(2.0, investment!!.quantity, 0.001)
        assertEquals(9_800.0, investment.cashBalance, 0.001)
        assertEquals(2, repository.getTransactions(id).size)
        database.close()
    }

    private fun open(name: String): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, name)
            .allowMainThreadQueries()
            .build()
}
