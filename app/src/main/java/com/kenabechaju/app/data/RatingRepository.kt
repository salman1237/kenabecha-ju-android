package com.kenabechaju.app.data

import com.kenabechaju.app.data.remote.ApiService
import com.kenabechaju.app.data.remote.dto.RatingCreateRequestDto
import com.kenabechaju.app.data.remote.dto.RatingDto
import com.kenabechaju.app.data.remote.dto.RatingEligibilityDto
import com.kenabechaju.app.data.remote.dto.ReportCreateRequestDto
import com.kenabechaju.app.data.remote.toApiFailure
import javax.inject.Inject
import javax.inject.Singleton

/** Ratings and reports share this repository since both are small,
 *  one-shot actions reachable from the same handful of screens
 *  (listing/shop/profile detail) — no reason to split them further. */
@Singleton
class RatingRepository @Inject constructor(private val api: ApiService) {
    suspend fun eligibility(listingId: String): AuthResult<RatingEligibilityDto> = resultOf {
        api.getRatingEligibility(listingId)
    }

    suspend fun rate(listingId: String, stars: Int, reviewText: String?): AuthResult<RatingDto> = resultOf {
        api.createRating(listingId, RatingCreateRequestDto(stars, reviewText))
    }

    suspend fun report(targetType: String, targetId: String, reasonCode: String, note: String?): AuthResult<Unit> = resultOf {
        api.createReport(ReportCreateRequestDto(targetType, targetId, reasonCode, note))
    }
}

private suspend fun <T> resultOf(block: suspend () -> T): AuthResult<T> = try {
    AuthResult.Success(block())
} catch (e: Exception) {
    AuthResult.Failure(e.toApiFailure())
}
