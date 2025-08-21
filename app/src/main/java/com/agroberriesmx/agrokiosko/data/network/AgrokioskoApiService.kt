package com.agroberriesmx.agrokiosko.data.network

import com.agroberriesmx.agrokiosko.data.network.response.ActivitiesResponse
import com.agroberriesmx.agrokiosko.data.network.response.PayrollResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface AgrokioskoApiService {
    @GET("ListNomlaborRecords/{worker}")
    suspend fun getPayroll(@Path("worker") worker:String): PayrollResponse

    @GET("ListNomlabordetRecords/{worker}")
    suspend fun getActivities(@Path("worker") worker:String): ActivitiesResponse
}