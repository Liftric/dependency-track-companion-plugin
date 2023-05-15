package com.liftric.tasks

import com.liftric.extensions.*
import com.liftric.service.DependencyTrack
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

abstract class UploadSBOMTask : DefaultTask() {
    @get:Input
    abstract val filePath: Property<String>

    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val url: Property<String>

    @get:Nested
    abstract val uploadSBOM: Property<UploadSBOMBuilder>

    @TaskAction
    fun uploadSBOMTask() {
        val filePathValue = filePath.get()
        val apiKeyValue = apiKey.get()
        val urlValue = url.get()
        val uploadSBOMValue = uploadSBOM.get().build()

        val cycloneDxReportFile = project.buildDir.resolve(filePathValue)
        if (cycloneDxReportFile.exists()) {
            val dt = DependencyTrack(apiKeyValue, urlValue)
            val response = dt.uploadSbom(cycloneDxReportFile, uploadSBOMValue)
            dt.waitForSbomAnalysis(response.token)
        } else {
            throw Exception("CycloneDX report file not found, run './gradlew cyclonedxBom'")
        }
    }
}
