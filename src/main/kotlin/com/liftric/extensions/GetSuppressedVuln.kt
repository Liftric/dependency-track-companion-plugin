package com.liftric.extensions

import org.gradle.api.provider.Property
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

data class GetSuppressedVuln(
    val projectName: String,
    val projectVersion: String,
)

@Suppress("MemberVisibilityCanBePrivate")
@ConfigDsl
class GetSuppressedVulnBuilder(@get:Internal val project: Project) {
    @get:Input
    val projectName: Property<String> = project.objects.property(String::class.java)

    @get:Input
    val projectVersion: Property<String> = project.objects.property(String::class.java)

    fun build(): GetSuppressedVuln = GetSuppressedVuln(
        projectName = projectName.get(),
        projectVersion = projectVersion.get(),
    )
}
