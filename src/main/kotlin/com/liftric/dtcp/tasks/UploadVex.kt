package com.liftric.dtcp.tasks

import com.liftric.dtcp.extensions.UploadVexBuilder
import com.liftric.dtcp.extensions.toNonNullPairList
import com.liftric.dtcp.service.DependencyTrack
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile

abstract class UploadVexTask : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val url: Property<String>

    @get:Nested
    abstract val uploadVex: Property<UploadVexBuilder>

    @TaskAction
    fun uploadVexTask() {
        val apiKeyValue = apiKey.get()
        val urlValue = url.get()
        val uploadVexValue = uploadVex.get().build()
        val outputFileValue = outputFile.get().asFile

        if (outputFileValue.exists()) {
            val formData = uploadVexValue.toNonNullPairList()
            val dt = DependencyTrack(apiKeyValue, urlValue)
            dt.uploadVex(outputFileValue, formData)
            println("Uploaded VEX file to Dependency-Track")
        } else {
            throw Exception("Vex file not found, run './gradlew generateVex'")
        }
    }
}
