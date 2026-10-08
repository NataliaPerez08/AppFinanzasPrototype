package com.appfinanzas.prototype.di

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.appfinanzas.prototype.data.local.AppDatabase
import com.appfinanzas.prototype.data.local.SeedData
import com.appfinanzas.prototype.data.room.RoomInvestmentRepository
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.security.AppLockManager
import com.appfinanzas.prototype.security.PinRepository
import com.appfinanzas.prototype.security.SharedPreferencesPinRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppContainer {

    @Volatile
    private var repository: InvestmentRepository? = null

    private var database: AppDatabase? = null
    private lateinit var pinRepository: PinRepository

    lateinit var appLockManager: AppLockManager
        private set

    val investmentRepository: InvestmentRepository
        get() = repository ?: error("AppContainer no inicializado. Llama a AppContainer.initialize(context)")

    fun initialize(context: Context) {
        if (!this::appLockManager.isInitialized) {
            pinRepository = SharedPreferencesPinRepository(context.applicationContext)
            appLockManager = AppLockManager(pinRepository).also { it.initialize() }
        }
        if (repository != null) return
        val database = AppDatabase.getInstance(context)
        this.database = database
        val repo = RoomInvestmentRepository(database)
        repository = repo
        CoroutineScope(Dispatchers.IO).launch {
            SeedData.seedIfNeeded(context)
            repo.repairLedgerIfNeeded()
        }
    }

    suspend fun resetAllData() {
        database?.clearAllTables()
        pinRepository.clearAll()
        appLockManager.resetToDisabled()
    }

    @VisibleForTesting
    fun setRepositoryForTest(repository: InvestmentRepository) {
        this.repository = repository
    }

    @VisibleForTesting
    fun resetForTest() {
        repository = null
    }
}
