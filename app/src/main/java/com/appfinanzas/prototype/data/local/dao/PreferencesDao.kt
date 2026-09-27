package com.appfinanzas.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appfinanzas.prototype.data.local.entity.PreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferencesDao {
    @Query("SELECT * FROM preferences WHERE key = :key")
    fun observe(key: String): Flow<PreferencesEntity?>

    @Query("SELECT * FROM preferences WHERE key = :key")
    suspend fun get(key: String): PreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PreferencesEntity)
}