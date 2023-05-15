package com.liftric.service

import com.liftric.extensions.UploadSBOM
import com.liftric.extensions.toNonNullPairList
import com.liftric.model.*
import io.ktor.client.call.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File

class DependencyTrack(apiKey: String, baseUrl: String) {

    private val client: ApiService = ApiService(apiKey)
    private val baseUrl: String = baseUrl

    fun getProject(projectName: String, projectVersion: String): Project = runBlocking {
        val url = "$baseUrl/api/v1/project/lookup?name=$projectName&version=$projectVersion"
        return@runBlocking client.getRequest(url).body()
    }

    fun getProjectComponentsById(id: String): List<Component> = runBlocking {
        val url = "$baseUrl/api/v1/component/project/$id"
        return@runBlocking client.getRequest(url).body()
    }

    fun getProjectFindingsById(id: String): List<Finding> = runBlocking {
        val url = "$baseUrl/api/v1/finding/project/$id?suppressed=true"
        return@runBlocking client.getRequest(url).body()
    }

    fun uploadVex(file: File, formData: List<Pair<String, String>>) = runBlocking {
        val url = "$baseUrl/api/v1/vex"
        client.uploadFileWithFormData(url, file, "vex", formData)
    }

    fun uploadSbom(
        file: File,
        uploadSBOM: UploadSBOM,
    ): UploadSBOMResponse = runBlocking {
        val formData = uploadSBOM.toNonNullPairList()
        val url = "$baseUrl/api/v1/bom"
        val res = client.uploadFileWithFormData(url, file, "bom", formData)
        return@runBlocking res.body()
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