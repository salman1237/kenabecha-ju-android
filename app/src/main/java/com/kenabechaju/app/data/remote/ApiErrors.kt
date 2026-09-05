package com.kenabechaju.app.data.remote

import com.kenabechaju.app.data.remote.dto.ApiErrorBody
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/** A resolved, user-facing message plus the machine-readable `code` (when
 *  the API sent one), for a screen that wants to localize it itself rather
 *  than just display the server's own English string. */
data class ApiFailure(val message: String, val code: String?)

private val errorJson = Json { ignoreUnknownKeys = true }

/**
 * Turns whatever a failed `suspend` API call threw into a message worth
 * showing, the same three-step fallback `lib/i18n/errors.ts` uses on web:
 * a real `code`+`detail` from the API, then just `detail` alone (FastAPI's
 * default shape has no `code`), then a generic network-failure message.
 */
fun Throwable.toApiFailure(): ApiFailure {
    if (this is HttpException) {
        val body = response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            val parsed = runCatching { errorJson.decodeFromString<ApiErrorBody>(body) }.getOrNull()
            val detail = parsed?.detail
            if (!detail.isNullOrBlank()) return ApiFailure(detail, parsed.code)
        }
        return ApiFailure("Something went wrong. Please try again.", null)
    }
    if (this is IOException) {
        return ApiFailure("Could not reach the server. Check your connection.", null)
    }
    return ApiFailure("Something went wrong. Please try again.", null)
}
