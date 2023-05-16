package com.liftric.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.liftric.model.*

class ApiService(private val dependencyTrackApiEndpoint: String) {
    private val client = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private suspend fun getAdminTeamUuid(): String {
        val teams: List<Team> = client.get("$dependencyTrackApiEndpoint/api/v1/team").body()
        val adminTeam = teams.firstOrNull { it.name == "Administrators" }
        return adminTeam?.uuid ?: throw Exception("Admin team not found.")
    }

    suspend fun getDependencyTrackAccessKey(): String {
        val adminUuid = getAdminTeamUuid()
        val response: KeyResponse = client.put("$dependencyTrackApiEndpoint/api/v1/team/$adminUuid/key").body()
        return response.key
    }
}
