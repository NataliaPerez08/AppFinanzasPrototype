package com.appfinanzas.prototype.di

import android.content.Context
import com.appfinanzas.prototype.data.local.AppDatabase
import com.appfinanzas.prototype.data.local.SeedData
import com.appfinanzas.prototype.data.room.RoomInvestmentRepository
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppContainer {

    @Volatile
    private var repository: InvestmentRepository? = null

    val investmentRepository: InvestmentRepository
        get() = repository ?: error("AppContainer no inicializado. Llama a AppContainer.initialize(context)")

    fun initialize(context: Context) {
        if (repository != null) return
        val database = AppDatabase.getInstance(context)
        repository = RoomInvestmentRepository(database)
        CoroutineScope(Dispatchers.IO).launch {
            SeedData.seedIfNeeded(context)
        }
    }
}