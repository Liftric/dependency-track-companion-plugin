package com.liftric.service

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import java.io.File

class ApiService(apiKey: String) {

    private val client = HttpClient(CIO) {
        expectSuccess = true
        install(DefaultRequest) {
            headers {
                append("X-Api-Key", apiKey)
            }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun uploadFileWithFormData(
        url: String,
        file: File,
        documentType: String,
        formData: List<Pair<String, String>>,
    ): HttpResponse {
        return client.submitFormWithBinaryData(
            url = url,
            formData = formData {
                formData.forEach { (key, value) ->
                    append(key, value)
                }
                append(documentType, file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "application/${file.extension}")
                    append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                })
            },
        )
    }

    suspend fun getRequest(url: String): HttpResponse {
        return client.get(url) {
            headers {
                append(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }
}
