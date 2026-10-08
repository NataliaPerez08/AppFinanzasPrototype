package com.appfinanzas.prototype.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.appfinanzas.prototype.data.local.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate1To2_preservesExistingData() {
        helper.createDatabase(TEST_DB, 1).use { database ->
            database.execSQL(
                "INSERT INTO institutions (id, name, kind) VALUES (1, 'GBM', 'Casa de Bolsa')",
            )
            database.execSQL(
                "INSERT INTO investments (id, name, description, symbol, type, institutionId, currency, " +
                    "currentPrice, priceChange, dailyChangePercentage, quantity, investedCapital, " +
                    "currentValue, dailyValueChange, returnPercentage, historyCsv) " +
                    "VALUES (1, 'VOO', 'Vanguard', 'VOO', 'ETF_FONDO', 1, 'MXN', " +
                    "100.0, 1.0, 1.0, 1.0, 800.0, 1000.0, 0.0, 25.0, '0.5,0.5')",
            )
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true, AppDatabase.MIGRATION_1_2)

        migrated.query(
            "SELECT name, investedCapital, currentValue, cashBalance, averageCost, realizedProfit " +
                "FROM investments WHERE id = 1",
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals("VOO", cursor.getString(0))
            assertEquals(800.0, cursor.getDouble(1), 0.001)
            assertEquals(1000.0, cursor.getDouble(2), 0.001)
            assertEquals(0.0, cursor.getDouble(3), 0.001)
            assertEquals(0.0, cursor.getDouble(4), 0.001)
            assertEquals(0.0, cursor.getDouble(5), 0.001)
        }
    }

    @Test
    fun migrate2To3_createsPriceHistoryTable() {
        helper.createDatabase(TEST_DB, 2).use { database ->
            database.execSQL(
                "INSERT INTO institutions (id, name, kind) VALUES (1, 'GBM', 'Casa de Bolsa')",
            )
            database.execSQL(
                "INSERT INTO investments (id, name, description, symbol, type, institutionId, currency, " +
                    "currentPrice, priceChange, dailyChangePercentage, quantity, investedCapital, " +
                    "currentValue, dailyValueChange, returnPercentage, historyCsv) " +
                    "VALUES (1, 'VOO', 'Vanguard', 'VOO', 'ETF_FONDO', 1, 'MXN', " +
                    "100.0, 1.0, 1.0, 1.0, 800.0, 1000.0, 0.0, 25.0, '0.5,0.5')",
            )
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 3, true, AppDatabase.MIGRATION_2_3)

        migrated.execSQL(
            "INSERT INTO price_history (investmentId, dateEpochDay, price) VALUES (1, 20089, 123.45)",
        )
        migrated.query("SELECT investmentId, price FROM price_history").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1L, cursor.getLong(0))
            assertEquals(123.45, cursor.getDouble(1), 0.001)
            assertEquals(1, cursor.count)
        }
    }

    @Test
    fun migrate3To4_addsProjectionConfigurationColumns() {
        helper.createDatabase(TEST_DB, 3).use { database ->
            database.execSQL(
                "INSERT INTO institutions (id, name, kind) VALUES (1, 'GBM', 'Casa de Bolsa')",
            )
            database.execSQL(
                "INSERT INTO investments (id, name, description, symbol, type, institutionId, currency, " +
                    "currentPrice, priceChange, dailyChangePercentage, quantity, investedCapital, " +
                    "currentValue, dailyValueChange, returnPercentage, historyCsv) " +
                    "VALUES (1, 'VOO', 'Vanguard', 'VOO', 'ETF_FONDO', 1, 'MXN', " +
                    "100.0, 1.0, 1.0, 1.0, 800.0, 1000.0, 0.0, 25.0, '0.5,0.5')",
            )
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 4, true, AppDatabase.MIGRATION_3_4)

        migrated.execSQL(
            "UPDATE investments SET projectionStrategy = 'MONTE_CARLO', " +
                "projectionReturn = 12.5, projectionVolatility = 18.0 WHERE id = 1",
        )
        migrated.query(
            "SELECT projectionStrategy, projectionReturn, projectionVolatility FROM investments WHERE id = 1",
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals("MONTE_CARLO", cursor.getString(0))
            assertEquals(12.5, cursor.getDouble(1), 0.001)
            assertEquals(18.0, cursor.getDouble(2), 0.001)
        }
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
