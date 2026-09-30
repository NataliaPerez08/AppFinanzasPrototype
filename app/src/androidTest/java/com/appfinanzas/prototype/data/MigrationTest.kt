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

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
