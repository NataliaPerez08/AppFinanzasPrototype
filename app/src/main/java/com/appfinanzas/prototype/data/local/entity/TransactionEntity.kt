package com.appfinanzas.prototype.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = InvestmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["investmentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("investmentId")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val investmentId: Long,
    val type: String,
    val dateEpochDay: Long,
    val quantity: Double,
    val price: Double,
    val commission: Double,
    val total: Double,
    val currency: String,
)