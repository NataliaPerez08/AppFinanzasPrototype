package com.appfinanzas.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.appfinanzas.prototype.data.local.entity.InstitutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstitutionDao {
    @Query("SELECT * FROM institutions ORDER BY name")
    fun observeAll(): Flow<List<InstitutionEntity>>

    @Query("SELECT * FROM institutions WHERE id = :id")
    suspend fun getById(id: Long): InstitutionEntity?

    @Query("SELECT * FROM institutions")
    suspend fun getAll(): List<InstitutionEntity>

    @Query("SELECT COUNT(*) FROM institutions")
    suspend fun getCount(): Long

    @Insert
    suspend fun insert(entity: InstitutionEntity): Long

    @Insert
    suspend fun insertAll(entities: List<InstitutionEntity>)
}