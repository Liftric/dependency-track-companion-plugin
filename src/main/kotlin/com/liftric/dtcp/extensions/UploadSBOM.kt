package com.liftric.dtcp.extensions

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

data class UploadSBOM(
    val autoCreate: Boolean,
    val project: String?,
    val projectName: String?,
    val projectVersion: String?,
    val parentName: String?,
    val parentVersion: String?,
    val parentUUID: String?,
)

@Suppress("MemberVisibilityCanBePrivate")
@ConfigDsl
class UploadSBOMBuilder(@get:Internal val proj: Project) {
    @get:Input
    @get:Optional
    val autoCreate: Property<Boolean> = proj.objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val project: Property<String?> = proj.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val projectName: Property<String?> = proj.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val projectVersion: Property<String?> = proj.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val parentName: Property<String?> = proj.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val parentVersion: Property<String?> = proj.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val parentUUID: Property<String?> = proj.objects.property(String::class.java)

    fun build(): UploadSBOM = UploadSBOM(
        autoCreate = autoCreate.get(),
        project = project.orNull,
        projectName = projectName.orNull,
        projectVersion = projectVersion.orNull,
        parentName = parentName.orNull,
        parentVersion = parentVersion.orNull,
        parentUUID = parentUUID.orNull,
    )
}

fun UploadSBOM.toNonNullPairList(): List<Pair<String, String>> = listOf(
    Pair("project", project),
    Pair("projectName", projectName),
    Pair("projectVersion", projectVersion),
    Pair("parentName", parentName),
    Pair("parentVersion", parentVersion),
    Pair("parentUUID", parentUUID),
).filterNot { it.second == null } as List<Pair<String, String>>
