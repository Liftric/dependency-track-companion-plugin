package com.liftric.extensions

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import kotlin.properties.Delegates

data class VexComponent(
    val purl: String,
    val vulnerability: VexVulnerability,
)

@Suppress("MemberVisibilityCanBePrivate")
@ConfigDsl
class VexComponentBuilder(@get:Internal val project: Project) {
    @get:Input
    val purl: Property<String> = project.objects.property(String::class.java)

    @get:Input
    var vulnerability: VexVulnerability by Delegates.notNull()

    fun vulnerability(action: VexVulnerabilityBuilder.() -> Unit) {
        vulnerability = VexVulnerabilityBuilder(project).apply(action).build()
    }

    fun build(): VexComponent = VexComponent(
        purl = purl.get(),
        vulnerability = vulnerability,
    )
}
