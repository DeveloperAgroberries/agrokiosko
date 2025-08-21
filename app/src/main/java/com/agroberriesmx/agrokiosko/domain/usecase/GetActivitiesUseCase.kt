package com.agroberriesmx.agrokiosko.domain.usecase

import com.agroberriesmx.agrokiosko.domain.Repository
import javax.inject.Inject

class GetActivitiesUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(worker: String) = repository.getActivities(worker)
}