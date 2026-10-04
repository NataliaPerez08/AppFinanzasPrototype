package com.appfinanzas.prototype.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.appfinanzas.prototype.data.local.AppDatabase
import com.appfinanzas.prototype.data.room.RoomInvestmentRepository
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomInvestmentRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: RoomInvestmentRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomInvestmentRepository(database)
    }

    @After
    fun tearDown() = database.close()

    private fun investment(
        institution: Institution,
        id: Long = 0,
        capital: Double = 100.0,
        quantity: Double = 1.0,
        cashBalance: Double = 0.0,
    ) = Investment(
        id = id,
        name = "VOO",
        description = "Vanguard",
        symbol = "VOO",
        type = InvestmentType.ETF_FONDO,
        institution = institution,
        currency = Currency.MXN,
        currentPrice = 100.0,
        priceChange = 1.0,
        dailyChangePercentage = 1.0,
        quantity = quantity,
        investedCapital = capital,
        currentValue = capital,
        dailyValueChange = 0.0,
        returnPercentage = 0.0,
        history = listOf(.5f, .5f),
        cashBalance = cashBalance,
    )

    private fun deposit(investmentId: Long, amount: Double, id: Long = 0) = Transaction(
        id = id,
        investmentId = investmentId,
        type = TransactionType.DEPOSITO,
        date = LocalDate.of(2026, 9, 21),
        quantity = 0.0,
        price = 0.0,
        commission = 0.0,
        total = amount,
        currency = Currency.MXN,
    )

    @Test
    fun settingsRoundTrip() = runTest {
        val settings = AppSettings(Currency.USD, 5.0, 3.0, 9.0, 21.5)
        repository.saveSettings(settings)

        assertEquals(settings, repository.observeSettings().first())
    }

    @Test
    fun addsInstitutionAndObservesIt() = runTest {
        repository.addInstitution(Institution(0, "Klar", "SOFIPO"))

        val institutions = repository.observeInstitutions().first()
        assertEquals(1, institutions.size)
        assertEquals("Klar", institutions.single().name)
    }

    @Test
    fun savesAndObservesInvestment() = runTest {
        val institutionId = repository.addInstitution(Institution(0, "GBM", "Casa de Bolsa"))
        val institution = repository.observeInstitutions().first().single()
        val id = repository.saveInvestment(investment(institution.copy(id = institutionId)))

        assertTrue(id > 0)
        assertEquals("VOO", repository.observeInvestment(id).first()!!.name)
    }

    @Test
    fun savesInvestmentWithOpeningTransactionAtomically() = runTest {
        val institutionId = repository.addInstitution(Institution(0, "GBM", "Casa de Bolsa"))
        val institution = repository.observeInstitutions().first().single().copy(id = institutionId)

        val id = repository.saveInvestmentWithOpeningTransaction(
            investment(institution, quantity = 0.0, cashBalance = 500.0),
            deposit(investmentId = 0, amount = 500.0),
        )

        val transactions = repository.getTransactions(id)
        assertEquals(1, transactions.size)
        assertEquals(500.0, transactions.single().total, 0.001)
        assertEquals(id, transactions.single().investmentId)
    }

    @Test
    fun deletesInvestmentAndCascadesTransactions() = runTest {
        val institutionId = repository.addInstitution(Institution(0, "GBM", "Casa de Bolsa"))
        val institution = repository.observeInstitutions().first().single().copy(id = institutionId)
        val id = repository.saveInvestmentWithOpeningTransaction(
            investment(institution, quantity = 0.0, cashBalance = 500.0),
            deposit(investmentId = 0, amount = 500.0),
        )

        repository.deleteInvestment(id)

        assertNull(repository.getInvestment(id))
        assertTrue(repository.getTransactions(id).isEmpty())
    }

    @Test
    fun deletesInvestmentWithoutTransactions() = runTest {
        val institutionId = repository.addInstitution(Institution(0, "GBM", "Casa de Bolsa"))
        val institution = repository.observeInstitutions().first().single().copy(id = institutionId)
        val id = repository.saveInvestment(investment(institution, quantity = 0.0, capital = 0.0))

        assertTrue(repository.getTransactions(id).isEmpty())

        repository.deleteInvestment(id)

        assertNull(repository.getInvestment(id))
        assertTrue(repository.getTransactions(id).isEmpty())
    }

    @Test
    fun updatesInvestmentPersistently() = runTest {
        val institutionId = repository.addInstitution(Institution(0, "GBM", "Casa de Bolsa"))
        val institution = repository.observeInstitutions().first().single().copy(id = institutionId)
        val id = repository.saveInvestment(investment(institution))

        repository.updateInvestment(repository.getInvestment(id)!!.copy(name = "CETES"))

        assertEquals("CETES", repository.getInvestment(id)!!.name)
    }

    @Test
    fun updatesTransactionAndInvestmentAtomically() = runTest {
        val institutionId = repository.addInstitution(Institution(0, "GBM", "Casa de Bolsa"))
        val institution = repository.observeInstitutions().first().single().copy(id = institutionId)
        val id = repository.saveInvestmentWithOpeningTransaction(
            investment(institution, quantity = 0.0, cashBalance = 500.0),
            deposit(investmentId = 0, amount = 500.0),
        )
        val transaction = repository.getTransactions(id).single()

        repository.updateTransactionWithInvestment(
            transaction.copy(total = 250.0),
            repository.getInvestment(id)!!.copy(cashBalance = 250.0),
        )

        assertEquals(250.0, repository.getTransactions(id).single().total, 0.001)
        assertEquals(250.0, repository.getInvestment(id)!!.cashBalance, 0.001)
    }

    @Test
    fun deletesTransactionAndUpdatesInvestmentAtomically() = runTest {
        val institutionId = repository.addInstitution(Institution(0, "GBM", "Casa de Bolsa"))
        val institution = repository.observeInstitutions().first().single().copy(id = institutionId)
        val id = repository.saveInvestmentWithOpeningTransaction(
            investment(institution, quantity = 0.0, cashBalance = 500.0),
            deposit(investmentId = 0, amount = 500.0),
        )
        val transaction = repository.getTransactions(id).single()

        repository.deleteTransactionWithInvestment(
            transaction.id,
            repository.getInvestment(id)!!.copy(cashBalance = 0.0),
        )

        assertTrue(repository.getTransactions(id).isEmpty())
        assertEquals(0.0, repository.getInvestment(id)!!.cashBalance, 0.001)
    }
}
