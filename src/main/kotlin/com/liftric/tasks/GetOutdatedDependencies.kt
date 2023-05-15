package com.liftric.tasks

import com.liftric.service.*
import com.liftric.extensions.*
import com.liftric.model.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

abstract class GetOutdatedDependenciesTask : DefaultTask() {
    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val url: Property<String>

    @get:Nested
    abstract val getOutdatedDependencies: Property<GetOutdatedDependenciesBuilder>

    @TaskAction
    fun getOutdatedDependenciesTask() {
        val apiKeyValue = apiKey.get()
        val urlValue = url.get()
        val inputData = getOutdatedDependencies.get().build()

        val dt = DependencyTrack(apiKeyValue, urlValue)

        val project = dt.getProject(inputData.projectName, inputData.projectVersion)
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
