package com.appfinanzas.prototype.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "investments",
    foreignKeys = [
        ForeignKey(
            entity = InstitutionEntity::class,
            parentColumns = ["id"],
            childColumns = ["institutionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("institutionId")],
)
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val symbol: String,
    val type: String,
    val institutionId: Long,
    val currency: String,
    val currentPrice: Double,
    val priceChange: Double,
    val dailyChangePercentage: Double,
    val quantity: Double,
    val investedCapital: Double,
    val currentValue: Double,
    val dailyValueChange: Double,
    val returnPercentage: Double,
    val historyCsv: String,
)