package com.liftric.dtcp.tasks

import com.liftric.dtcp.service.DependencyTrack
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

abstract class UploadVexTask : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val url: Property<String>

    @get:Input
    @get:Optional
    abstract val projectUUID: Property<String>

    @get:Input
    @get:Optional
    abstract val projectName: Property<String>

    @get:Input
    @get:Optional
    abstract val projectVersion: Property<String>

    @TaskAction
    fun uploadVexTask() {
        val apiKeyValue = apiKey.get()
        val urlValue = url.get()
        val outputFileValue = outputFile.get().asFile
        val projectUUIDValue = projectUUID.orNull
        val projectNameValue = projectName.orNull
        val projectVersionValue = projectVersion.orNull

        if (projectUUIDValue == null) {
            if (projectNameValue == null || projectVersionValue == null) {
                throw GradleException("Either projectUUID or projectName and projectVersion must be set")
            }
        }

        val dt = DependencyTrack(apiKeyValue, urlValue)
        val response = dt.uploadVex(
            file = outputFileValue,
            projectUUID = projectUUIDValue,
            projectName = projectNameValue,
            projectVersion = projectVersionValue,
        )
        dt.waitForTokenCompletion(response.token)
        logger.info("Uploaded VEX file to Dependency-Track")
    }
}
