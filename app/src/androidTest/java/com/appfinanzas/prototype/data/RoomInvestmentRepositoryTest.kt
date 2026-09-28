package com.appfinanzas.prototype.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.appfinanzas.prototype.data.local.AppDatabase
import com.appfinanzas.prototype.data.local.entity.InstitutionEntity
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.data.room.RoomInvestmentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
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

    @Test
    fun settingsRoundTrip() = runTest {
        val settings = AppSettings(Currency.USD, 5.0, 3.0, 9.0)
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
        val investment = com.appfinanzas.prototype.domain.model.Investment(
            id = 0,
            name = "VOO",
            description = "Vanguard",
            symbol = "VOO",
            type = InvestmentType.ETF_FONDO,
            institution = institution.copy(id = institutionId),
            currency = Currency.USD,
            currentPrice = 100.0,
            priceChange = 1.0,
            dailyChangePercentage = 1.0,
            quantity = 1.0,
            investedCapital = 100.0,
            currentValue = 100.0,
            dailyValueChange = 0.0,
            returnPercentage = 0.0,
            history = listOf(.5f, .5f),
        )
        val id = repository.saveInvestment(investment)

        assertTrue(id > 0)
        assertEquals("VOO", repository.observeInvestment(id).first()!!.name)
    }
}
