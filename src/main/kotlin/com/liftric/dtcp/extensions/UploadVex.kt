package com.liftric.dtcp.extensions

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

data class UploadVex(
    val project: String?,
    val projectName: String?,
    val projectVersion: String?,
)

@Suppress("MemberVisibilityCanBePrivate")
@ConfigDsl
class UploadVexBuilder(@get:Internal val proj: Project) {
    @get:Input
    @get:Optional
    val project: Property<String?> = proj.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val projectName: Property<String?> = proj.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val projectVersion: Property<String?> = proj.objects.property(String::class.java)

    fun build(): UploadVex = UploadVex(
        project = this.project.orNull,
        projectName = this.projectName.orNull,
        projectVersion = this.projectVersion.orNull,
    )
}
