package com.appfinanzas.prototype.data.room

import androidx.room.withTransaction
import com.appfinanzas.prototype.data.local.AppDatabase
import com.appfinanzas.prototype.data.local.entity.PreferencesEntity
import com.appfinanzas.prototype.data.mapper.InstitutionMapper
import com.appfinanzas.prototype.data.mapper.InvestmentMapper
import com.appfinanzas.prototype.data.mapper.PricePointMapper
import com.appfinanzas.prototype.data.mapper.TransactionMapper
import com.appfinanzas.prototype.data.mapper.toFloatList
import com.appfinanzas.prototype.domain.ledger.LedgerCalculator
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.PricePoint
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RoomInvestmentRepository(
    private val database: AppDatabase,
) : InvestmentRepository {

    private val institutionDao = database.institutionDao()
    private val investmentDao = database.investmentDao()
    private val transactionDao = database.transactionDao()
    private val preferencesDao = database.preferencesDao()
    private val pricePointDao = database.pricePointDao()

    override fun observeInvestments(): Flow<List<Investment>> =
        combine(
            investmentDao.observeAll(),
            institutionDao.observeAll(),
        ) { investments, institutions ->
            val byId = institutions.associateBy { it.id }
            investments.map { entity ->
                val institution = byId[entity.institutionId]?.let { InstitutionMapper.toDomain(it) }
                    ?: Institution(id = entity.institutionId, name = "—", kind = "")
                InvestmentMapper.toDomain(entity, institution)
            }
        }

    override fun observeInvestment(investmentId: Long): Flow<Investment?> =
        combine(
            investmentDao.observeById(investmentId),
            institutionDao.observeAll(),
        ) { entity, institutions ->
            entity?.let { e ->
                val institution = institutions.firstOrNull { it.id == e.institutionId }?.let { InstitutionMapper.toDomain(it) }
                    ?: Institution(id = e.institutionId, name = "—", kind = "")
                InvestmentMapper.toDomain(e, institution)
            }
        }

    override fun observeTransactions(investmentId: Long): Flow<List<Transaction>> =
        transactionDao.observeByInvestment(investmentId)
            .map { entities -> entities.map { TransactionMapper.toDomain(it) } }

    override fun observePriceHistory(investmentId: Long): Flow<List<PricePoint>> =
        pricePointDao.observeByInvestment(investmentId)
            .map { entities -> entities.map { PricePointMapper.toDomain(it) } }

    override fun observePriceHistory(): Flow<Map<Long, List<PricePoint>>> =
        pricePointDao.observeAll()
            .map { entities ->
                entities
                    .groupBy { it.investmentId }
                    .mapValues { (_, points) -> points.map { PricePointMapper.toDomain(it) } }
            }

    override fun observePortfolioHistory(): Flow<List<Float>> =
        preferencesDao.observe(AppDatabase.PORTFOLIO_HISTORY_KEY)
            .map { it?.value?.toFloatList() ?: emptyList() }

    override fun observeInstitutions(): Flow<List<Institution>> =
        institutionDao.observeAll().map { entities -> entities.map { InstitutionMapper.toDomain(it) } }

    override suspend fun getInvestment(investmentId: Long): Investment? {
        val entity = investmentDao.getById(investmentId) ?: return null
        val institution = institutionDao.getById(entity.institutionId)
            ?.let { InstitutionMapper.toDomain(it) }
            ?: Institution(id = entity.institutionId, name = "—", kind = "")
        return InvestmentMapper.toDomain(entity, institution)
    }

    override suspend fun getAllInvestments(): List<Investment> {
        val institutions = institutionDao.getAll().associateBy { it.id }
        return investmentDao.getAll().map { entity ->
            val institution = institutions[entity.institutionId]?.let { InstitutionMapper.toDomain(it) }
                ?: Institution(id = entity.institutionId, name = "—", kind = "")
            InvestmentMapper.toDomain(entity, institution)
        }
    }

    override suspend fun getTransaction(transactionId: Long): Transaction? =
        transactionDao.getById(transactionId)?.let { TransactionMapper.toDomain(it) }

    override suspend fun getTransactions(investmentId: Long): List<Transaction> =
        transactionDao.getByInvestment(investmentId).map { TransactionMapper.toDomain(it) }

    override suspend fun getPriceHistory(investmentId: Long): List<PricePoint> =
        pricePointDao.getByInvestment(investmentId).map { PricePointMapper.toDomain(it) }

    override suspend fun saveInvestment(investment: Investment): Long =
        investmentDao.insert(InvestmentMapper.toEntity(investment))

    override suspend fun saveInvestmentWithOpeningTransaction(
        investment: Investment,
        openingTransaction: Transaction,
    ): Long = database.withTransaction {
        val id = investmentDao.insert(InvestmentMapper.toEntity(investment))
        transactionDao.insert(TransactionMapper.toEntity(openingTransaction.copy(investmentId = id)))
        id
    }

    override suspend fun updateInvestment(investment: Investment) {
        investmentDao.update(InvestmentMapper.toEntity(investment))
    }

    override suspend fun deleteInvestment(investmentId: Long) {
        val entity = investmentDao.getById(investmentId) ?: return
        investmentDao.delete(entity)
    }

    override suspend fun saveTransaction(transaction: Transaction): Long =
        transactionDao.insert(TransactionMapper.toEntity(transaction))

    override suspend fun savePricePoint(pricePoint: PricePoint): Long =
        pricePointDao.insert(PricePointMapper.toEntity(pricePoint))

    override suspend fun saveTransactionWithInvestment(
        transaction: Transaction,
        investment: Investment,
    ): Long = database.withTransaction {
        val id = transactionDao.insert(TransactionMapper.toEntity(transaction))
        investmentDao.update(InvestmentMapper.toEntity(investment))
        id
    }

    override suspend fun updateTransactionWithInvestment(
        transaction: Transaction,
        investment: Investment,
    ) {
        database.withTransaction {
            transactionDao.update(TransactionMapper.toEntity(transaction))
            investmentDao.update(InvestmentMapper.toEntity(investment))
        }
    }

    override suspend fun deleteTransactionWithInvestment(
        transactionId: Long,
        investment: Investment,
    ) {
        database.withTransaction {
            val entity = transactionDao.getById(transactionId) ?: return@withTransaction
            transactionDao.delete(entity)
            investmentDao.update(InvestmentMapper.toEntity(investment))
        }
    }

    suspend fun recomputeAllLedgers() {
        database.withTransaction {
            val institutions = institutionDao.getAll().associateBy { it.id }
            investmentDao.getAll().forEach { entity ->
                val institution = institutions[entity.institutionId]?.let { InstitutionMapper.toDomain(it) }
                    ?: Institution(id = entity.institutionId, name = "—", kind = "")
                val investment = InvestmentMapper.toDomain(entity, institution)
                val transactions = transactionDao.getByInvestment(entity.id).map { TransactionMapper.toDomain(it) }
                val state = LedgerCalculator.recompute(investment.currentPrice, transactions)
                investmentDao.update(
                    InvestmentMapper.toEntity(
                        investment.copy(
                            quantity = state.quantity,
                            averageCost = state.averageCost,
                            cashBalance = state.cashBalance,
                            investedCapital = state.investedCapital,
                            realizedProfit = state.realizedProfit,
                            currentPrice = state.currentPrice,
                            currentValue = state.currentValue,
                            returnPercentage = state.returnPercentage,
                        ),
                    ),
                )
            }
        }
    }

    override fun observeSettings(): Flow<AppSettings> =
        combine(
            preferencesDao.observe(AppSettings.KEY_BASE_CURRENCY),
            preferencesDao.observe(AppSettings.KEY_ESTIMATED_INFLATION),
            preferencesDao.observe(AppSettings.KEY_ESTIMATED_ISR),
            preferencesDao.observe(AppSettings.KEY_EXPECTED_RETURN),
            preferencesDao.observe(AppSettings.KEY_USD_TO_MXN_RATE),
            preferencesDao.observe(AppSettings.KEY_ESTIMATED_VOLATILITY),
            preferencesDao.observe(AppSettings.KEY_MONTHLY_CONTRIBUTION),
        ) { values ->
            val base = values[0]?.value
            val inflation = values[1]?.value
            val isr = values[2]?.value
            val expectedReturn = values[3]?.value
            val usdToMxnRate = values[4]?.value
            val volatility = values[5]?.value
            val monthlyContribution = values[6]?.value
            AppSettings(
                baseCurrency = base?.let { runCatching { Currency.valueOf(it) }.getOrNull() }
                    ?: AppSettings().baseCurrency,
                estimatedInflation = inflation?.toDoubleOrNull() ?: AppSettings().estimatedInflation,
                estimatedIsr = isr?.toDoubleOrNull() ?: AppSettings().estimatedIsr,
                expectedReturn = expectedReturn?.toDoubleOrNull() ?: AppSettings().expectedReturn,
                usdToMxnRate = usdToMxnRate?.toDoubleOrNull() ?: AppSettings().usdToMxnRate,
                estimatedVolatility = volatility?.toDoubleOrNull() ?: AppSettings().estimatedVolatility,
                monthlyContribution = monthlyContribution?.toDoubleOrNull() ?: AppSettings().monthlyContribution,
            )
        }

    override suspend fun saveSettings(settings: AppSettings) {
        preferencesDao.upsert(
            PreferencesEntity(
                key = AppSettings.KEY_BASE_CURRENCY,
                value = settings.baseCurrency.name,
            ),
        )
        preferencesDao.upsert(
            PreferencesEntity(
                key = AppSettings.KEY_ESTIMATED_INFLATION,
                value = settings.estimatedInflation.toString(),
            ),
        )
        preferencesDao.upsert(
            PreferencesEntity(
                key = AppSettings.KEY_ESTIMATED_ISR,
                value = settings.estimatedIsr.toString(),
            ),
        )
        preferencesDao.upsert(
            PreferencesEntity(
                key = AppSettings.KEY_EXPECTED_RETURN,
                value = settings.expectedReturn.toString(),
            ),
        )
        preferencesDao.upsert(
            PreferencesEntity(
                key = AppSettings.KEY_ESTIMATED_VOLATILITY,
                value = settings.estimatedVolatility.toString(),
            ),
        )
        preferencesDao.upsert(
            PreferencesEntity(
                key = AppSettings.KEY_MONTHLY_CONTRIBUTION,
                value = settings.monthlyContribution.toString(),
            ),
        )
        preferencesDao.upsert(
            PreferencesEntity(
                key = AppSettings.KEY_USD_TO_MXN_RATE,
                value = settings.usdToMxnRate.toString(),
            ),
        )
    }

    override suspend fun addInstitution(institution: Institution): Long =
        institutionDao.insert(InstitutionMapper.toEntity(institution))

    override suspend fun repairLedgerIfNeeded() {
        val repaired = preferencesDao.observe(AppDatabase.LEDGER_REPAIRED_KEY).first()
        if (repaired?.value == "true") return
        recomputeAllLedgers()
        preferencesDao.upsert(PreferencesEntity(AppDatabase.LEDGER_REPAIRED_KEY, "true"))
    }
}
