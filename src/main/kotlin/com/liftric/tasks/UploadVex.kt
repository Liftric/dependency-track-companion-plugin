package com.liftric.tasks

import com.liftric.extensions.*
import com.liftric.service.DependencyTrack
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

abstract class UploadVexTask : DefaultTask() {
    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val url: Property<String>

    @get:Input
    abstract val outputPath: Property<String>

    @get:Input
    abstract val outputFilename: Property<String>

    @get:Nested
    abstract val uploadVex: Property<UploadVexBuilder>

    @TaskAction
    fun uploadVexTask() {
        val apiKeyValue = apiKey.get()
        val urlValue = url.get()
        val uploadVexValue = uploadVex.get().build()
        val outputPathValue = outputPath.get()
        val outputFilenameValue = outputFilename.get()
        val outputFile = "$outputPathValue$outputFilenameValue"

        val vexFile = project.buildDir.resolve("${outputFile}.json")
        if (vexFile.exists()) {
            val formData = uploadVexValue.toNonNullPairList()
            val dt = DependencyTrack(apiKeyValue, urlValue)
            dt.uploadVex(vexFile, formData)
            println("Uploaded VEX file to Dependency-Track")
        } else {
            throw Exception("Vex file not found, run './gradlew generateVex'")
        }
    }
}
