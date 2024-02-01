package com.liftric.dtcp.service

import com.liftric.dtcp.model.ProjectData
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * This IgnoreErrorApiService class is a workaround for a specific issue when running Dependency Track with Docker Compose
 * for integration testing, with authentication disabled (`ALPINE_ENFORCE_AUTHENTICATION=false`).
 *
 * Under these circumstances, Dependency Track behaves inconsistently - some API endpoints operate as expected, while others
 * return a 500 status code. Notably, this issue arises when attempting to create a new project.
 *
 * To circumvent this, IgnoreErrorApiService is used to handle project creation requests, with `expectSuccess` set to false
 * by default.
 *
 * This setup allows us to run integration tests seamlessly, even with the aforementioned Docker configuration.
 */

class IgnoreErrorApiService(
    private val dependencyTrackApiEndpoint: String,
    private val dependencyTrackAccessKey: String,
) {
    private val client = HttpClient(CIO) {
        expectSuccess = false
    }

    suspend fun createProject(name: String) {
        val projectData = ProjectData(
            "Test Author",
            "Test Publisher",
            "Test Group",
            name,
            "This is a test project",
            "1.0.0",
            "APPLICATION",
            true
        )

        client.put("${dependencyTrackApiEndpoint}/api/v1/project") {
            headers {
                append("X-Api-Key", dependencyTrackAccessKey)
                append("Content-Type", "application/json")
            }
            setBody(Json.encodeToString(projectData))
        }
    }
}
