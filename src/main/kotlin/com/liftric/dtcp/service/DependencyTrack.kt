package com.liftric.dtcp.service

import com.liftric.dtcp.extensions.UploadSBOMBuilder
import com.liftric.dtcp.extensions.UploadVexBuilder
import com.liftric.dtcp.model.*
import io.ktor.client.call.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File

class DependencyTrack(apiKey: String, baseUrl: String) {

    private val client: ApiService = ApiService(apiKey)
    private val baseUrl: String = baseUrl

    fun getProject(projectName: String, projectVersion: String): Project = runBlocking {
        val url = "$baseUrl/api/v1/project/lookup?name=$projectName&version=$projectVersion"
        client.getRequest(url).body()
    }

    fun analyzeProjectFindings(projectUuid: String): UploadSBOMResponse = runBlocking {
        val url = "$baseUrl/api/v1/finding/project/$projectUuid/analyze"
        client.postRequest(url).body()
    }

    fun getProjectComponentsById(id: String): List<Component> = runBlocking {
        val url = "$baseUrl/api/v1/component/project/$id"
        client.getRequest(url).body()
    }

    fun getProjectFindingsById(id: String): List<Finding> = runBlocking {
        val url = "$baseUrl/api/v1/finding/project/$id?suppressed=true"
        client.getRequest(url).body()
    }

    fun uploadVex(file: File, uploadVex: UploadVexBuilder) = runBlocking {
        val url = "$baseUrl/api/v1/vex"
        client.uploadFileWithFormData(url, file, "vex") {
            uploadVex.project.orNull?.let {
                append("project", it)
            }
            uploadVex.projectName.orNull?.let {
                append("projectName", it)
            }
            uploadVex.projectVersion.orNull?.let {
                append("projectVersion", it)
            }
        }
    }

    fun uploadSbom(
        file: File,
        uploadSBOM: UploadSBOMBuilder,
    ): UploadSBOMResponse = runBlocking {
        val url = "$baseUrl/api/v1/bom"
        val res = client.uploadFileWithFormData(url, file, "bom") {
            uploadSBOM.autoCreate.orNull?.let {
                append("autoCreate", it)
            }
            uploadSBOM.project.orNull?.let {
                append("project", it)
            }
            uploadSBOM.projectName.orNull?.let {
                append("projectName", it)
            }
            uploadSBOM.projectVersion.orNull?.let {
                append("projectVersion", it)
            }
            uploadSBOM.parentName.orNull?.let {
                append("parentName", it)
            }
            uploadSBOM.parentVersion.orNull?.let {
                append("parentVersion", it)
            }
            uploadSBOM.parentUUID.orNull?.let {
                append("parentUUID", it)
            }
        }
        res.body()
    }

    fun waitForSbomAnalysis(token: String) = runBlocking {
        val url = "$baseUrl/api/v1/bom/token/$token"
        var response: SBOMProcessingResponse

        do {
            println("Waiting for SBOM Analysis Processing...")
            delay(2000)
            response = client.getRequest(url).body()
        } while (response.processing)
        println("Analysis is complete.")
    }
}
