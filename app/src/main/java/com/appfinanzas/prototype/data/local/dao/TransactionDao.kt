package com.appfinanzas.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.appfinanzas.prototype.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE investmentId = :investmentId ORDER BY dateEpochDay DESC, id DESC")
    fun observeByInvestment(investmentId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE investmentId = :investmentId ORDER BY dateEpochDay ASC, id ASC")
    suspend fun getByInvestment(investmentId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<TransactionEntity>

    @Insert
    suspend fun insert(entity: TransactionEntity): Long

    @Update
    suspend fun update(entity: TransactionEntity)

    @Delete
    suspend fun delete(entity: TransactionEntity)
}
