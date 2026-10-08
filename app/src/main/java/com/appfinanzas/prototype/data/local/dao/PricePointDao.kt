package com.appfinanzas.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.appfinanzas.prototype.data.local.entity.PricePointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PricePointDao {
    @Query("SELECT * FROM price_history WHERE investmentId = :investmentId ORDER BY dateEpochDay ASC, id ASC")
    fun observeByInvestment(investmentId: Long): Flow<List<PricePointEntity>>

    @Query("SELECT * FROM price_history ORDER BY investmentId ASC, dateEpochDay ASC, id ASC")
    fun observeAll(): Flow<List<PricePointEntity>>

    @Query("SELECT * FROM price_history WHERE investmentId = :investmentId ORDER BY dateEpochDay ASC, id ASC")
    suspend fun getByInvestment(investmentId: Long): List<PricePointEntity>

    @Query("SELECT * FROM price_history")
    suspend fun getAll(): List<PricePointEntity>

    @Insert
    suspend fun insert(entity: PricePointEntity): Long
}
