package com.kenabechaju.app.data

import com.kenabechaju.app.data.remote.ApiService
import com.kenabechaju.app.data.remote.dto.DepartmentDto
import com.kenabechaju.app.data.remote.dto.HallDto
import com.kenabechaju.app.data.remote.dto.SessionOptionDto
import javax.inject.Inject
import javax.inject.Singleton

/** Halls/departments/sessions rarely change — fetched once per process and
 *  kept in memory, matching how little the web app's own signup form
 *  re-fetches them either. */
@Singleton
class ReferenceRepository @Inject constructor(private val api: ApiService) {
    private var halls: List<HallDto>? = null
    private var departments: List<DepartmentDto>? = null
    private var sessions: List<SessionOptionDto>? = null

    suspend fun halls(): List<HallDto> = halls ?: api.getHalls().also { halls = it }
    suspend fun departments(): List<DepartmentDto> = departments ?: api.getDepartments().also { departments = it }
    suspend fun sessions(): List<SessionOptionDto> = sessions ?: api.getSessions().also { sessions = it }
}
