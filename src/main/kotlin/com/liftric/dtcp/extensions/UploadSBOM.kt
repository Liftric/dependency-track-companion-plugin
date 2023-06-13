package com.liftric.dtcp.extensions

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

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
}
