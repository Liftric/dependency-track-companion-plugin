package com.liftric.dtcp.tasks

import com.liftric.dtcp.service.DependencyTrack
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class AnalyzeProjectTask : DefaultTask() {
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
    fun analyzeProjectTask() {
        val apiKeyValue = apiKey.get()
        val urlValue = url.get()

        val projectUUIDValue = projectUUID.orNull
        val projectNameValue = projectName.orNull
        val projectVersionValue = projectVersion.orNull

        val dt = DependencyTrack(apiKeyValue, urlValue)

        val uuid = when {
            projectUUIDValue != null -> projectUUIDValue
            projectNameValue != null && projectVersionValue != null -> dt.getProject(
                projectNameValue,
                projectVersionValue
            ).uuid

            else -> throw GradleException("Either projectUUID or projectName and projectVersion must be set")
        }

        val response = dt.analyzeProjectFindings(uuid)
        dt.waitForTokenCompletion(response.token)
    }
}
