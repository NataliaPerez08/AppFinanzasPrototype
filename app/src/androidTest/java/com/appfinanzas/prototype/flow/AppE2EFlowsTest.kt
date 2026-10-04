package com.appfinanzas.prototype.flow

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.assertTextContains
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.appfinanzas.prototype.MainActivity
import com.appfinanzas.prototype.data.local.AppDatabase
import com.appfinanzas.prototype.data.room.RoomInvestmentRepository
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.usecase.AddInvestmentResult
import com.appfinanzas.prototype.domain.usecase.AddInvestmentUseCase
import com.appfinanzas.prototype.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppE2EFlowsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    companion object {
        private lateinit var database: AppDatabase
        private lateinit var repository: RoomInvestmentRepository

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
            repository = RoomInvestmentRepository(database)
            AppContainer.setRepositoryForTest(repository)
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            AppContainer.resetForTest()
            database.close()
        }
    }

    @Before
    fun resetData() {
        runBlocking {
            database.clearAllTables()
            repository.addInstitution(Institution(0, "GBM", "Casa de Bolsa"))
        }
    }

    @Test
    fun createInvestment_throughUi_appearsInPortfolio() {
        waitForText("+ AGREGAR INVERSIÓN")
        composeRule.onNodeWithText("+ AGREGAR INVERSIÓN").performClick()
        waitForText("NUEVA INVERSIÓN")

        clickScrolling("ACCIÓN")
        clickScrolling("GBM")
        typeInto("Símbolo / nombre", "Apple")
        typeInto("Símbolo", "AAPL")
        clickScrolling("MXN")
        typeInto("Valor inicial", "10000")
        clickScrolling("GUARDAR INVERSIÓN")

        waitForText("APPLE")

        composeRule.onNodeWithText("INVERSIONES").performClick()
        waitForText("APPLE")

        assertTrue(runBlocking { repository.observeInvestments().first() }.isNotEmpty())
    }

    @Test
    fun cancelNewInvestment_doesNotPersistData() {
        waitForText("+ AGREGAR INVERSIÓN")
        composeRule.onNodeWithText("+ AGREGAR INVERSIÓN").performClick()
        waitForText("NUEVA INVERSIÓN")

        clickScrolling("CANCELAR")

        waitForText("NO HAY INVERSIONES")
        assertTrue(runBlocking { repository.observeInvestments().first() }.isEmpty())
    }

    @Test
    fun deleteInvestment_fromDetail_returnsToEmptyPortfolio() {
        seedInvestment()

        composeRule.onNodeWithText("INVERSIONES").performClick()
        waitForText("APPLE")
        composeRule.onNodeWithText("APPLE").performClick()
        waitForText("ELIMINAR INVERSIÓN")

        clickScrolling("ELIMINAR INVERSIÓN")
        waitForText("ELIMINAR")
        composeRule.onNodeWithText("ELIMINAR").performClick()

        waitForText("NO HAY INVERSIONES")
        assertTrue(runBlocking { repository.observeInvestments().first() }.isEmpty())
    }

    @Test
    fun cancelDeleteDialog_keepsInvestment() {
        seedInvestment()

        composeRule.onNodeWithText("INVERSIONES").performClick()
        waitForText("APPLE")
        composeRule.onNodeWithText("APPLE").performClick()
        waitForText("ELIMINAR INVERSIÓN")

        clickScrolling("ELIMINAR INVERSIÓN")
        waitForText("ELIMINAR")
        composeRule.onNodeWithText("CANCELAR").performClick()

        waitForText("VALOR ACTUAL")
        assertTrue(runBlocking { repository.observeInvestments().first() }.isNotEmpty())
    }

    @Test
    fun registerBuy_updatesDetailQuantity() {
        seedInvestment()

        composeRule.onNodeWithText("INVERSIONES").performClick()
        waitForText("APPLE")
        composeRule.onNodeWithText("APPLE").performClick()
        waitForText("REGISTRAR MOVIMIENTO")

        clickScrolling("REGISTRAR MOVIMIENTO")
        waitForText("REGISTRAR MOVIMIENTO")
        clickScrolling("COMPRA")
        typeInto("Cantidad", "2")
        typeInto("Precio MXN", "100")
        clickScrolling("GUARDAR MOVIMIENTO")

        waitForText("2.00")
    }

    @Test
    fun cancelNewTransaction_keepsInvestmentUnchanged() {
        val investmentId = seedInvestment()

        composeRule.onNodeWithText("INVERSIONES").performClick()
        waitForText("APPLE")
        composeRule.onNodeWithText("APPLE").performClick()
        waitForText("REGISTRAR MOVIMIENTO")

        clickScrolling("REGISTRAR MOVIMIENTO")
        waitForText("REGISTRAR MOVIMIENTO")
        clickScrolling("CANCELAR")

        waitForText("VALOR ACTUAL")
        assertTrue(
            runBlocking { repository.getTransactions(investmentId).size == 1 },
        )
    }

    @Test
    fun recreatingActivity_preservesDashboardData() {
        seedInvestment()

        composeRule.runOnUiThread { composeRule.activity.recreate() }

        waitForText("VALOR ACTUAL")
        waitForText("\$10,000.00 MXN")
    }

    @Test
    fun recreatingActivity_preservesNewInvestmentForm() {
        waitForText("+ AGREGAR INVERSIÓN")
        composeRule.onNodeWithText("+ AGREGAR INVERSIÓN").performClick()
        waitForText("NUEVA INVERSIÓN")

        clickScrolling("ACCIÓN")
        clickScrolling("GBM")
        typeInto("Símbolo / nombre", "Apple")
        typeInto("Símbolo", "AAPL")
        clickScrolling("MXN")
        typeInto("Valor inicial", "10000")

        composeRule.runOnUiThread { composeRule.activity.recreate() }

        waitForText("NUEVA INVERSIÓN")
        composeRule.onNodeWithContentDescription("Símbolo / nombre").assertTextContains("Apple")
        composeRule.onNodeWithContentDescription("Símbolo").assertTextContains("AAPL")
        composeRule.onNodeWithContentDescription("Valor inicial").assertTextContains("10000")
    }

    @Test
    fun recreatingActivity_preservesNewTransactionForm() {
        seedInvestment()

        composeRule.onNodeWithText("INVERSIONES").performClick()
        waitForText("APPLE")
        composeRule.onNodeWithText("APPLE").performClick()
        waitForText("REGISTRAR MOVIMIENTO")
        clickScrolling("REGISTRAR MOVIMIENTO")
        waitForText("REGISTRAR MOVIMIENTO")
        clickScrolling("COMPRA")
        typeInto("Cantidad", "2")
        typeInto("Precio MXN", "100")

        composeRule.runOnUiThread { composeRule.activity.recreate() }

        waitForText("REGISTRAR MOVIMIENTO")
        composeRule.onNodeWithContentDescription("Cantidad").assertTextContains("2")
        composeRule.onNodeWithContentDescription("Precio MXN").assertTextContains("100")
    }

    private fun seedInvestment(): Long = runBlocking {
        val institutionId = repository.observeInstitutions().first().first().id
        val result = AddInvestmentUseCase(repository).execute(FlowTestHarness.investmentForm(institutionId))
        (result as AddInvestmentResult.Success).investmentId
    }

    private fun seedInvestmentWithBuy(quantity: String, price: String): Long {
        val id = seedInvestment()
        runBlocking {
            AddTransactionUseCase(repository)
                .execute(id, FlowTestHarness.transactionForm(TransactionType.COMPRA, quantity, price))
        }
        return id
    }

    @Test
    fun editTransaction_throughUi_updatesQuantity() {
        seedInvestmentWithBuy(quantity = "2", price = "100")

        composeRule.onNodeWithText("INVERSIONES").performClick()
        waitForText("APPLE")
        composeRule.onNodeWithText("APPLE").performClick()
        waitForText("COMPRA")
        clickScrolling("COMPRA")
        waitForText("EDITAR MOVIMIENTO")

        composeRule.onNodeWithContentDescription("Cantidad").performScrollTo().performTextReplacement("5")
        clickScrolling("GUARDAR CAMBIOS")

        waitForText("5.00")
    }

    @Test
    fun deleteTransaction_throughUi_removesMovement() {
        val investmentId = seedInvestmentWithBuy(quantity = "2", price = "100")

        composeRule.onNodeWithText("INVERSIONES").performClick()
        waitForText("APPLE")
        composeRule.onNodeWithText("APPLE").performClick()
        waitForText("COMPRA")
        clickScrolling("COMPRA")
        waitForText("EDITAR MOVIMIENTO")

        clickScrolling("ELIMINAR MOVIMIENTO")
        waitForText("ELIMINAR")
        composeRule.onNodeWithText("ELIMINAR").performClick()

        waitForText("DEPOSITO")
        assertTrue(
            runBlocking {
                repository.getTransactions(investmentId).none { it.type == TransactionType.COMPRA }
            },
        )
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun clickScrolling(text: String) {
        composeRule.onNodeWithText(text).performScrollTo().performClick()
    }

    private fun typeInto(label: String, value: String) {
        composeRule.onNodeWithContentDescription(label).performScrollTo().performTextInput(value)
    }
}
