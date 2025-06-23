package com.liftric.dtcp.service

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class ApiService(apiKey: String, disableStrictTLS: Boolean = false) {

    private val trustAllManager = if (disableStrictTLS) {
        object: X509TrustManager {
            @Suppress("kotlin:S4830")
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) { /* NOOP */ }

            @Suppress("kotlin:S4830")
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) { /* NOOP */ }

            override fun getAcceptedIssuers(): Array<X509Certificate>? = null
        }
    } else {
        null
    }

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
        engine {
            https {
                trustManager = trustAllManager
            }
        }
    }

    suspend fun uploadFileWithFormData(
        url: String,
        file: File,
        documentType: String,
        formData: FormBuilder.() -> Unit,
    ): HttpResponse {
        return client.submitFormWithBinaryData(
            url = url,
            formData = formData {
                formData()
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

    suspend fun postRequest(url: String): HttpResponse {
        return client.post(url) {
            headers {
                append(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }

    suspend fun <T> putRequest(url: String, body: T, serializer: KSerializer<T>): HttpResponse {
        val jsonBody = Json.encodeToString(serializer, body)
        return client.put(url) {
            headers {
                append(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
    }
}
