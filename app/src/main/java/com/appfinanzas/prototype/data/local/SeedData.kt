package com.appfinanzas.prototype.data.local

import android.content.Context
import com.appfinanzas.prototype.data.local.entity.InstitutionEntity

object SeedData {

    private val defaultInstitutions = listOf(
        InstitutionEntity(name = "GBM", kind = "Casa de Bolsa"),
        InstitutionEntity(name = "CETES Directo", kind = "Renta fija"),
        InstitutionEntity(name = "NU", kind = "SOFIPO"),
        InstitutionEntity(name = "BBVA", kind = "Banco"),
        InstitutionEntity(name = "Mercado Pago", kind = "Otra"),
    )

    suspend fun seedIfNeeded(context: Context) {
        val database = AppDatabase.getInstance(context)
        if (database.institutionDao().getCount() == 0L) {
            database.institutionDao().insertAll(defaultInstitutions)
        }
    }
}