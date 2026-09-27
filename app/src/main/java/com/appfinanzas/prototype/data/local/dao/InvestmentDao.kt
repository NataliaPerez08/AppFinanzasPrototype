package com.appfinanzas.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.appfinanzas.prototype.data.local.entity.InvestmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments ORDER BY id")
    fun observeAll(): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments WHERE id = :id")
    fun observeById(id: Long): Flow<InvestmentEntity?>

    @Query("SELECT * FROM investments WHERE id = :id")
    suspend fun getById(id: Long): InvestmentEntity?

    @Insert
    suspend fun insert(entity: InvestmentEntity): Long

    @Update
    suspend fun update(entity: InvestmentEntity)
}