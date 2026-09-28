package com.appfinanzas.prototype.data.room

import com.appfinanzas.prototype.data.local.AppDatabase
import com.appfinanzas.prototype.data.local.entity.PreferencesEntity
import com.appfinanzas.prototype.data.mapper.InstitutionMapper
import com.appfinanzas.prototype.data.mapper.InvestmentMapper
import com.appfinanzas.prototype.data.mapper.TransactionMapper
import com.appfinanzas.prototype.data.mapper.toFloatList
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RoomInvestmentRepository(
    database: AppDatabase,
) : InvestmentRepository {

    private val institutionDao = database.institutionDao()
    private val investmentDao = database.investmentDao()
    private val transactionDao = database.transactionDao()
    private val preferencesDao = database.preferencesDao()

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

    override fun observePortfolioHistory(): Flow<List<Float>> =
        preferencesDao.observe(AppDatabase.PORTFOLIO_HISTORY_KEY)
            .map { it?.value?.toFloatList() ?: emptyList() }

    override fun observeInstitutions(): Flow<List<Institution>> =
        institutionDao.observeAll().map { entities -> entities.map { InstitutionMapper.toDomain(it) } }

    override suspend fun saveInvestment(investment: Investment): Long =
        investmentDao.insert(InvestmentMapper.toEntity(investment))

    override suspend fun updateInvestment(investment: Investment) {
        investmentDao.update(InvestmentMapper.toEntity(investment))
    }

    override suspend fun saveTransaction(transaction: Transaction): Long =
        transactionDao.insert(TransactionMapper.toEntity(transaction))

    override fun observeSettings(): Flow<AppSettings> =
        combine(
            preferencesDao.observe(AppSettings.KEY_BASE_CURRENCY),
            preferencesDao.observe(AppSettings.KEY_ESTIMATED_INFLATION),
            preferencesDao.observe(AppSettings.KEY_ESTIMATED_ISR),
        ) { base, inflation, isr ->
            AppSettings(
                baseCurrency = base?.value?.let { runCatching { Currency.valueOf(it) }.getOrNull() }
                    ?: AppSettings().baseCurrency,
                estimatedInflation = inflation?.value?.toDoubleOrNull() ?: AppSettings().estimatedInflation,
                estimatedIsr = isr?.value?.toDoubleOrNull() ?: AppSettings().estimatedIsr,
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
    }

    override suspend fun addInstitution(institution: Institution): Long =
        institutionDao.insert(InstitutionMapper.toEntity(institution))
}