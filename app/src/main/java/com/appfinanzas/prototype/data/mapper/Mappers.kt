package com.appfinanzas.prototype.data.mapper

import com.appfinanzas.prototype.data.local.entity.InstitutionEntity
import com.appfinanzas.prototype.data.local.entity.InvestmentEntity
import com.appfinanzas.prototype.data.local.entity.TransactionEntity
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import java.time.LocalDate

fun List<Float>.toCsv(): String = joinToString(",") { it.toString() }

fun String.toFloatList(): List<Float> =
    if (isBlank()) emptyList() else split(",").mapNotNull { it.toFloatOrNull() }

object InstitutionMapper {
    fun toDomain(entity: InstitutionEntity): Institution =
        Institution(id = entity.id, name = entity.name, kind = entity.kind)
}

object InvestmentMapper {
    fun toDomain(entity: InvestmentEntity, institution: Institution): Investment =
        Investment(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            symbol = entity.symbol,
            type = InvestmentType.valueOf(entity.type),
            institution = institution,
            currency = Currency.valueOf(entity.currency),
            currentPrice = entity.currentPrice,
            priceChange = entity.priceChange,
            dailyChangePercentage = entity.dailyChangePercentage,
            quantity = entity.quantity,
            investedCapital = entity.investedCapital,
            currentValue = entity.currentValue,
            dailyValueChange = entity.dailyValueChange,
            returnPercentage = entity.returnPercentage,
            history = entity.historyCsv.toFloatList(),
        )

    fun toEntity(domain: Investment): InvestmentEntity =
        InvestmentEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            symbol = domain.symbol,
            type = domain.type.name,
            institutionId = domain.institution.id,
            currency = domain.currency.name,
            currentPrice = domain.currentPrice,
            priceChange = domain.priceChange,
            dailyChangePercentage = domain.dailyChangePercentage,
            quantity = domain.quantity,
            investedCapital = domain.investedCapital,
            currentValue = domain.currentValue,
            dailyValueChange = domain.dailyValueChange,
            returnPercentage = domain.returnPercentage,
            historyCsv = domain.history.toCsv(),
        )
}

object TransactionMapper {
    fun toDomain(entity: TransactionEntity): Transaction =
        Transaction(
            id = entity.id,
            investmentId = entity.investmentId,
            type = TransactionType.valueOf(entity.type),
            date = LocalDate.ofEpochDay(entity.dateEpochDay),
            quantity = entity.quantity,
            price = entity.price,
            commission = entity.commission,
            total = entity.total,
            currency = Currency.valueOf(entity.currency),
        )

    fun toEntity(domain: Transaction): TransactionEntity =
        TransactionEntity(
            id = domain.id,
            investmentId = domain.investmentId,
            type = domain.type.name,
            dateEpochDay = domain.date.toEpochDay(),
            quantity = domain.quantity,
            price = domain.price,
            commission = domain.commission,
            total = domain.total,
            currency = domain.currency.name,
        )
}