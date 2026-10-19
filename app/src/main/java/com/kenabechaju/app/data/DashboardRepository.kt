package com.kenabechaju.app.data

import com.kenabechaju.app.data.remote.ApiService
import com.kenabechaju.app.data.remote.dto.ActivityPointDto
import com.kenabechaju.app.data.remote.dto.DashboardStatsDto
import com.kenabechaju.app.data.remote.dto.ListingDto
import com.kenabechaju.app.data.remote.dto.UserProfileDto
import com.kenabechaju.app.data.remote.toApiFailure
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(private val api: ApiService) {
    suspend fun stats(): AuthResult<DashboardStatsDto> = resultOf { api.getDashboardStats() }

    suspend fun activity(days: Int = 30): AuthResult<List<ActivityPointDto>> = resultOf { api.getDashboardActivity(days) }

    suspend fun savedListings(): AuthResult<List<ListingDto>> = resultOf { api.getSavedListings() }

    suspend fun userProfile(id: String): AuthResult<UserProfileDto> = resultOf { api.getUserProfile(id) }
}

private suspend fun <T> resultOf(block: suspend () -> T): AuthResult<T> = try {
    AuthResult.Success(block())
} catch (e: Exception) {
    AuthResult.Failure(e.toApiFailure())
}
