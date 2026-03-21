package com.agroberriesmx.agrokiosko.data

import android.util.Log
import com.agroberriesmx.agrokiosko.data.network.AgrokioskoApiService
import com.agroberriesmx.agrokiosko.domain.Repository
import com.agroberriesmx.agrokiosko.domain.model.ActivitiesModel
import com.agroberriesmx.agrokiosko.domain.model.PayrollModel
import javax.inject.Inject

class RepositoryImpl @Inject constructor(private val apiService: AgrokioskoApiService): Repository {
    override suspend fun getPayroll(worker: String): List<PayrollModel>? {
        return runCatching { apiService.getPayroll(worker) }
            .mapCatching { response -> response.payroll.map{ it.toDomain()} }
            .onFailure { Log.i("ErrorApi", "Ha ocurrido un error ${it.message}") }
            .getOrNull()
    }

    override suspend fun getActivities(worker: String): List<ActivitiesModel>? {
        return runCatching { apiService.getActivities(worker) }
            .mapCatching { response -> response.activities.map{ it.toDomain() } }
            //.onFailure { Log.i("ErrorApi", "Ha ocurrido un error ${it.message}") }
            .onFailure {
                // ⭐ CAMBIO A Log.e Y SE INCLUYE EL STACK TRACE
                Log.e("RepositoryImpl", "ERROR en getActivities: ${it.message}", it)
            }
            .getOrNull()
    }
}