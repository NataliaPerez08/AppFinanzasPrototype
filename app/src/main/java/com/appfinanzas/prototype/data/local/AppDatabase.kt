package com.appfinanzas.prototype.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.appfinanzas.prototype.data.local.dao.InstitutionDao
import com.appfinanzas.prototype.data.local.dao.InvestmentDao
import com.appfinanzas.prototype.data.local.dao.PreferencesDao
import com.appfinanzas.prototype.data.local.dao.TransactionDao
import com.appfinanzas.prototype.data.local.entity.InstitutionEntity
import com.appfinanzas.prototype.data.local.entity.InvestmentEntity
import com.appfinanzas.prototype.data.local.entity.PreferencesEntity
import com.appfinanzas.prototype.data.local.entity.TransactionEntity

@Database(
    entities = [
        InstitutionEntity::class,
        InvestmentEntity::class,
        TransactionEntity::class,
        PreferencesEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun institutionDao(): InstitutionDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun transactionDao(): TransactionDao
    abstract fun preferencesDao(): PreferencesDao

    companion object {
        const val PORTFOLIO_HISTORY_KEY = "portfolio_history"
        const val LEDGER_REPAIRED_KEY = "ledger_repaired_v2"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE investments ADD COLUMN cashBalance REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE investments ADD COLUMN averageCost REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE investments ADD COLUMN realizedProfit REAL NOT NULL DEFAULT 0")
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_finanzas.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}