package com.liftric.dtcp.tasks

import com.liftric.dtcp.model.CreateProject
import com.liftric.dtcp.model.ProjectTag
import com.liftric.dtcp.service.DependencyTrack
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class CreateProject : DefaultTask() {
    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val url: Property<String>

    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    @get:Optional
    abstract val projectVersion: Property<String>

    @get:Input
    @get:Optional
    abstract val projectActive: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val projectTags: ListProperty<ProjectTag>

    @get:Input
    @get:Optional
    abstract val parentUUID: Property<String>

    @get:Input
    @get:Optional
    abstract val ignoreProjectAlreadyExists: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val disableStrictTLS: Property<Boolean>

    @TaskAction
    fun createProjectTask() {
        val dt = DependencyTrack(apiKey.get(), url.get(), disableStrictTLS.getOrElse(false))

        val project = CreateProject(
            name = projectName.get(),
            version = projectVersion.orNull,
            active = projectActive.orNull ?: true,
            tags = projectTags.getOrElse(emptyList()),
            parent = parentUUID.orNull?.let { CreateProject.Parent(it) }
        )

        try {
            dt.createProject(project)
        } catch (e: Exception) {
            if (ignoreProjectAlreadyExists.getOrElse(false) && e.message?.contains("already exists") == true) {
                logger.info("Project already exists, ignoring")
                return
            }
            logger.error("Error creating project: ${e.message}")
            throw e
        }
    }
}
