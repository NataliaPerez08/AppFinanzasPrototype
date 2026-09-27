package com.appfinanzas.prototype.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "institutions")
data class InstitutionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: String,
)