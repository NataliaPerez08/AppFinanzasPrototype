package com.appfinanzas.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.appfinanzas.prototype.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE investmentId = :investmentId ORDER BY dateEpochDay DESC, id DESC")
    fun observeByInvestment(investmentId: Long): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insert(entity: TransactionEntity): Long
}