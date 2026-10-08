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
import com.appfinanzas.prototype.data.local.dao.PricePointDao
import com.appfinanzas.prototype.data.local.dao.TransactionDao
import com.appfinanzas.prototype.data.local.entity.InstitutionEntity
import com.appfinanzas.prototype.data.local.entity.InvestmentEntity
import com.appfinanzas.prototype.data.local.entity.PreferencesEntity
import com.appfinanzas.prototype.data.local.entity.PricePointEntity
import com.appfinanzas.prototype.data.local.entity.TransactionEntity

@Database(
    entities = [
        InstitutionEntity::class,
        InvestmentEntity::class,
        TransactionEntity::class,
        PreferencesEntity::class,
        PricePointEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun institutionDao(): InstitutionDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun transactionDao(): TransactionDao
    abstract fun preferencesDao(): PreferencesDao
    abstract fun pricePointDao(): PricePointDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `price_history` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`investmentId` INTEGER NOT NULL, " +
                        "`dateEpochDay` INTEGER NOT NULL, " +
                        "`price` REAL NOT NULL, " +
                        "FOREIGN KEY(`investmentId`) REFERENCES `investments`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_price_history_investmentId` " +
                        "ON `price_history` (`investmentId`)",
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE investments ADD COLUMN projectionStrategy TEXT")
                db.execSQL("ALTER TABLE investments ADD COLUMN projectionReturn REAL")
                db.execSQL("ALTER TABLE investments ADD COLUMN projectionVolatility REAL")
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { instance = it }
            }
    }
}