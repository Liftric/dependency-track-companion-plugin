package com.liftric.dtcp.extensions

import org.gradle.api.provider.Property
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

data class GetOutdatedDependencies(
    val projectName: String,
    val projectVersion: String,
)

@Suppress("MemberVisibilityCanBePrivate")
@ConfigDsl
class GetOutdatedDependenciesBuilder(@get:Internal val project: Project) {
    @get:Input
    val projectName: Property<String> = project.objects.property(String::class.java)

    @get:Input
    val projectVersion: Property<String> = project.objects.property(String::class.java)

    fun build(): GetOutdatedDependencies = GetOutdatedDependencies(
        projectName = projectName.get(),
        projectVersion = projectVersion.get(),
    )
}
