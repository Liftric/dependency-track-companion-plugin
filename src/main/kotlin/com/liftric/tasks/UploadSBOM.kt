package com.liftric.tasks

import com.liftric.extensions.*
import com.liftric.service.DependencyTrack
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested

abstract class UploadSBOMTask : DefaultTask() {
    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val url: Property<String>

    @get:Nested
    abstract val uploadSBOM: Property<UploadSBOMBuilder>

    @TaskAction
    fun uploadSBOMTask() {
        val inputFileValue = inputFile.get().asFile
        val apiKeyValue = apiKey.get()
        val urlValue = url.get()
        val uploadSBOMValue = uploadSBOM.get().build()

        if (inputFileValue.exists()) {
            val dt = DependencyTrack(apiKeyValue, urlValue)
            val response = dt.uploadSbom(inputFileValue, uploadSBOMValue)
            dt.waitForSbomAnalysis(response.token)
        } else {
            throw Exception("CycloneDX report file not found, run './gradlew cyclonedxBom'")
        }
    }
}
