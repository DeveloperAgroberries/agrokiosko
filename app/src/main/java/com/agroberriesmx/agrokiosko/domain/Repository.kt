package com.agroberriesmx.agrokiosko.domain

import com.agroberriesmx.agrokiosko.domain.model.ActivitiesModel
import com.agroberriesmx.agrokiosko.domain.model.PayrollModel

interface Repository {
    suspend fun getPayroll(worker: String): List<PayrollModel>?
    suspend fun getActivities(worker: String): List<ActivitiesModel>?
}