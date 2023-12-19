package com.liftric.dtcp.tasks

import com.liftric.dtcp.model.Component
import com.liftric.dtcp.model.DirectDependency
import com.liftric.dtcp.service.DependencyTrack
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class GetOutdatedDependenciesTask : DefaultTask() {
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
    fun getOutdatedDependenciesTask() {
        val apiKeyValue = apiKey.get()
        val urlValue = url.get()
        val projectUUIDValue = projectUUID.orNull
        val projectNameValue = projectName.orNull
        val projectVersionValue = projectVersion.orNull

        val dt = DependencyTrack(apiKeyValue, urlValue)

        val project = when {
            projectUUIDValue != null -> dt.getProject(projectUUIDValue)
            projectNameValue != null && projectVersionValue != null -> dt.getProject(
                projectNameValue,
                projectVersionValue
            )

            else -> throw GradleException("Either projectUUID or projectName and projectVersion must be set")
        }

        if (project.directDependencies == null) {
            throw GradleException("Project does not have direct dependencies")
        }

        val directDependencies = Json {
            ignoreUnknownKeys = true
        }.decodeFromString<List<DirectDependency>>(project.directDependencies)


        // Component API is currently limited to 100 results per request
        var offset = 0
        val allComponents = mutableListOf<Component>()
        do {
            val components = dt.getProjectComponentsById(project.uuid + "?offset=${offset}")
            allComponents.addAll(components)
            offset += components.size
        } while (components.size == 100)

        printOutdatedDirectDependencies(allComponents, directDependencies)
    }

    private fun printOutdatedDirectDependencies(
        components: List<Component>,
        directDependencies: List<DirectDependency>,
    ) {
        components.forEach { component ->
            directDependencies.forEach { dependency ->
                if (component.purl == dependency.purl && component.version != component.repositoryMeta?.latestVersion) {
                    component.repositoryMeta?.let {
                        printColorfully(component.purl, component.version, it.latestVersion)
                    }
                }
            }
        }
    }

    private fun printColorfully(purl: String, version: String, latestVersion: String) {
        val red = "\u001b[31m"
        val green = "\u001b[32m"
        val reset = "\u001b[0m"
        println("${purl}: $red${version}$reset -> $green${latestVersion}$reset")
    }
}
