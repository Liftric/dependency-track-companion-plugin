package com.liftric.dtcp.service

import com.liftric.dtcp.model.*
import io.ktor.client.call.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File

class DependencyTrack(apiKey: String, private val baseUrl: String) {

    private val client: ApiService = ApiService(apiKey)

    fun getProject(projectName: String, projectVersion: String): Project = runBlocking {
        val url = "$baseUrl/api/v1/project/lookup?name=$projectName&version=$projectVersion"
        client.getRequest(url).body()
    }

    fun getProject(projectUUID: String): Project = runBlocking {
        val url = "$baseUrl/api/v1/project/$projectUUID"
        client.getRequest(url).body()
    }

    fun analyzeProjectFindings(projectUUID: String): TaskTokenResponse = runBlocking {
        val url = "$baseUrl/api/v1/finding/project/$projectUUID/analyze"
        client.postRequest(url).body()
    }

    fun getProjectComponentsById(projectUUID: String): List<Component> = runBlocking {
        val url = "$baseUrl/api/v1/component/project/$projectUUID"
        client.getRequest(url).body()
    }

    fun getProjectFindingsById(projectUUID: String): List<Finding> = runBlocking {
        val url = "$baseUrl/api/v1/finding/project/$projectUUID?suppressed=true"
        client.getRequest(url).body()
    }

    fun uploadVex(
        file: File,
        projectUUID: String?,
        projectName: String?,
        projectVersion: String?,
    ): TaskTokenResponse = runBlocking {
        val url = "$baseUrl/api/v1/vex"
        client.uploadFileWithFormData(url, file, "vex") {
            projectUUID?.let {
                append("projectUUID", it)
            }
            projectName?.let {
                append("projectName", it)
            }
            projectVersion?.let {
                append("projectVersion", it)
            }
        }.body()
    }

    fun uploadSbom(
        file: File,
        autoCreate: Boolean,
        projectUUID: String?,
        projectName: String?,
        projectVersion: String?,
        parentUUID: String?,
        parentName: String?,
        parentVersion: String?,
    ): TaskTokenResponse = runBlocking {
        val url = "$baseUrl/api/v1/bom"
        val res = client.uploadFileWithFormData(url, file, "bom") {
            append("autoCreate", autoCreate)
            projectUUID?.let {
                append("project", it)
            }
            projectName?.let {
                append("projectName", it)
            }
            projectVersion?.let {
                append("projectVersion", it)
            }
            parentUUID?.let {
                append("parentUUID", it)
            }
            parentName?.let {
                append("parentName", it)
            }
            parentVersion?.let {
                append("parentVersion", it)
            }
        }
        res.body()
    }

    fun waitForTokenCompletion(token: String) = runBlocking {
        val url = "$baseUrl/api/v1/event/token/$token"
        var response: EventTokenResponse

        do {
            println("Waiting for task completion...")
            delay(2000)
            response = client.getRequest(url).body()
        } while (response.processing)
        println("Task is complete.")
    }

    fun createProject(project: CreateProject) = runBlocking {
        val url = "$baseUrl/api/v1/project"
        client.putRequest(url, project, CreateProject.serializer())
    }
}
