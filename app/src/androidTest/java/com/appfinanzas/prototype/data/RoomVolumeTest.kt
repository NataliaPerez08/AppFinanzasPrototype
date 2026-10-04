package com.appfinanzas.prototype.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.room.Room
import com.appfinanzas.prototype.data.local.AppDatabase
import com.appfinanzas.prototype.data.local.entity.InstitutionEntity
import com.appfinanzas.prototype.data.local.entity.InvestmentEntity
import com.appfinanzas.prototype.data.local.entity.TransactionEntity
import com.appfinanzas.prototype.data.room.RoomInvestmentRepository
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.TransactionType
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class RoomVolumeTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomInvestmentRepository

    private val institutionCount = 20
    private val investmentCount = 100
    private val transactionsPerInvestment = 10

    @Before
    fun setup() {
        val context = androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomInvestmentRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun seed20Institutions100Investments1000Transactions() = runBlocking {
        val start = System.nanoTime()

        val institutionIds = (1..institutionCount).map { i ->
            database.institutionDao().insert(
                InstitutionEntity(name = "Institución $i", kind = "Casa de Bolsa")
            )
        }

        val investmentIds = (1..investmentCount).map { i ->
            val instId = institutionIds[i % institutionCount]
            database.investmentDao().insert(
                InvestmentEntity(
                    name = "Inversión $i",
                    description = "Desc $i",
                    symbol = "SYM$i",
                    type = InvestmentType.ACCION.name,
                    institutionId = instId,
                    currency = Currency.MXN.name,
                    currentPrice = 100.0 + i,
                    priceChange = 1.0,
                    dailyChangePercentage = 0.5,
                    quantity = 10.0,
                    investedCapital = 1000.0,
                    currentValue = 1000.0,
                    dailyValueChange = 5.0,
                    returnPercentage = 0.0,
                    historyCsv = "",
                )
            )
        }

        val baseDate = LocalDate.of(2026, 1, 1)
        investmentIds.forEachIndexed { idx, invId ->
            repeat(transactionsPerInvestment) { t ->
                val day = baseDate.plusDays(t.toLong())
                database.transactionDao().insert(
                    TransactionEntity(
                        investmentId = invId,
                        type = if (t % 3 == 0) TransactionType.COMPRA.name else TransactionType.DIVIDENDO.name,
                        dateEpochDay = day.toEpochDay(),
                        quantity = if (t % 3 == 0) 1.0 else 0.0,
                        price = if (t % 3 == 0) 100.0 else 0.0,
                        commission = 0.0,
                        total = if (t % 3 == 0) 100.0 else 10.0,
                        currency = Currency.MXN.name,
                    )
                )
            }
        }

        val elapsedMs = (System.nanoTime() - start) / 1_000_000.0
        println("SEED_TIME_MS=$elapsedMs")

        assertTrue("seed took ${elapsedMs}ms, expected < 30000ms", elapsedMs < 30_000.0)

        val invCount = database.investmentDao().getAll().size
        val txCount = database.transactionDao().getAll().size
        val instCount = database.institutionDao().getAll().size

        assertTrue("expected $investmentCount investments, got $invCount", invCount == investmentCount)
        assertTrue("expected ${investmentCount * transactionsPerInvestment} transactions, got $txCount",
            txCount == investmentCount * transactionsPerInvestment)
        assertTrue("expected $institutionCount institutions, got $instCount", instCount == institutionCount)
    }

    @Test
    fun queryAllInvestmentsWith1000TransactionsUnder100ms() = runBlocking {
        seedVolumeData()

        val start = System.nanoTime()
        val investments = database.investmentDao().getAll()
        val elapsedMs = (System.nanoTime() - start) / 1_000_000.0

        println("QUERY_ALL_INVESTMENTS_MS=$elapsedMs")
        assertTrue("query took ${elapsedMs}ms, expected < 100ms", elapsedMs < 100.0)
        assertTrue("expected 100 investments", investments.size == 100)
    }

    @Test
    fun queryTransactionsByInvestmentUnder50msEach() = runBlocking {
        seedVolumeData()

        val investmentIds = database.investmentDao().getAll().map { it.id }
        var totalMs = 0.0

        investmentIds.forEach { id ->
            val start = System.nanoTime()
            val txs = database.transactionDao().getByInvestment(id)
            val elapsedMs = (System.nanoTime() - start) / 1_000_000.0
            totalMs += elapsedMs
            assertTrue("expected 10 transactions for investment $id, got ${txs.size}", txs.size == 10)
        }

        val avgMs = totalMs / investmentIds.size
        println("AVG_QUERY_TX_PER_INVESTMENT_MS=$avgMs")
        assertTrue("avg query took ${avgMs}ms, expected < 50ms", avgMs < 50.0)
    }

    @Test
    fun observeInvestmentsFlowEmitsUnder200ms() = runBlocking {
        seedVolumeData()

        val start = System.nanoTime()
        val investments = repository.observeInvestments().first()
        val elapsedMs = (System.nanoTime() - start) / 1_000_000.0

        println("OBSERVE_INVESTMENTS_FIRST_MS=$elapsedMs")
        assertTrue("first emission took ${elapsedMs}ms, expected < 200ms", elapsedMs < 200.0)
        assertTrue("expected 100 investments", investments.size == 100)
    }

    @Test
    fun recomputeAllLedgersWith1000TransactionsUnder500ms() = runBlocking {
        seedVolumeData()

        val start = System.nanoTime()
        repository.recomputeAllLedgers()
        val elapsedMs = (System.nanoTime() - start) / 1_000_000.0

        println("RECOMPUTE_ALL_LEDGERS_MS=$elapsedMs")
        assertTrue("recompute took ${elapsedMs}ms, expected < 500ms", elapsedMs < 500.0)
    }

    private suspend fun seedVolumeData() {
        val institutionIds = (1..institutionCount).map { i ->
            database.institutionDao().insert(
                InstitutionEntity(name = "Institución $i", kind = "Casa de Bolsa")
            )
        }

        val investmentIds = (1..investmentCount).map { i ->
            val instId = institutionIds[i % institutionCount]
            database.investmentDao().insert(
                InvestmentEntity(
                    name = "Inversión $i",
                    description = "Desc $i",
                    symbol = "SYM$i",
                    type = InvestmentType.ACCION.name,
                    institutionId = instId,
                    currency = Currency.MXN.name,
                    currentPrice = 100.0 + i,
                    priceChange = 1.0,
                    dailyChangePercentage = 0.5,
                    quantity = 10.0,
                    investedCapital = 1000.0,
                    currentValue = 1000.0,
                    dailyValueChange = 5.0,
                    returnPercentage = 0.0,
                    historyCsv = "",
                )
            )
        }

        val baseDate = LocalDate.of(2026, 1, 1)
        investmentIds.forEach { invId ->
            repeat(transactionsPerInvestment) { t ->
                val day = baseDate.plusDays(t.toLong())
                database.transactionDao().insert(
                    TransactionEntity(
                        investmentId = invId,
                        type = if (t % 3 == 0) TransactionType.COMPRA.name else TransactionType.DIVIDENDO.name,
                        dateEpochDay = day.toEpochDay(),
                        quantity = if (t % 3 == 0) 1.0 else 0.0,
                        price = if (t % 3 == 0) 100.0 else 0.0,
                        commission = 0.0,
                        total = if (t % 3 == 0) 100.0 else 10.0,
                        currency = Currency.MXN.name,
                    )
                )
            }
        }
    }
}
