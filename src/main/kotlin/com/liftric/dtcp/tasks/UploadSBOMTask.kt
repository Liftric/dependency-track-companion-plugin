package com.liftric.dtcp.tasks

import com.liftric.dtcp.service.DependencyTrack
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

abstract class UploadSBOMTask : DefaultTask() {
    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val url: Property<String>

    @get:Input
    @get:Optional
    abstract val autoCreate: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val projectUUID: Property<String>

    @get:Input
    @get:Optional
    abstract val projectName: Property<String>

    @get:Input
    @get:Optional
    abstract val projectVersion: Property<String>

    @get:Input
    @get:Optional
    abstract val parentUUID: Property<String>

    @get:Input
    @get:Optional
    abstract val parentName: Property<String>

    @get:Input
    @get:Optional
    abstract val parentVersion: Property<String>

    @TaskAction
    fun uploadSBOMTask() {
        val inputFileValue = inputFile.get().asFile
        val apiKeyValue = apiKey.get()
        val urlValue = url.get()
        val autoCreateValue = autoCreate.get()
        val projectUUIDValue = projectUUID.orNull
        val projectNameValue = projectName.orNull
        val projectVersionValue = projectVersion.orNull
        val parentNameValue = parentName.orNull
        val parentVersionValue = parentVersion.orNull
        val parentUUIDValue = parentUUID.orNull

        if (projectUUIDValue == null && (projectNameValue == null && projectVersionValue == null)) {
            throw GradleException("Either projectUUID or projectName and projectVersion must be set")
        }

        val dt = DependencyTrack(apiKeyValue, urlValue)
        val response = dt.uploadSbom(
            file = inputFileValue,
            autoCreate = autoCreateValue,
            projectUUID = projectUUIDValue,
            projectName = projectNameValue,
            projectVersion = projectVersionValue,
            parentUUID = parentUUIDValue,
            parentName = parentNameValue,
            parentVersion = parentVersionValue,
        )
        dt.waitForTokenCompletion(response.token)
    }
}
