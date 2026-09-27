package com.appfinanzas.prototype.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun institutionDao(): InstitutionDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun transactionDao(): TransactionDao
    abstract fun preferencesDao(): PreferencesDao

    companion object {
        const val PORTFOLIO_HISTORY_KEY = "portfolio_history"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_finanzas.db",
                ).build().also { instance = it }
            }
    }
}