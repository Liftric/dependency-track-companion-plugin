package com.liftric.dtcp.extensions

import com.liftric.dtcp.model.ProjectTag
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class DepTrackCompanionExtension(val project: Project) {
    abstract val url: Property<String>
    abstract val apiKey: Property<String>

    abstract val inputFile: RegularFileProperty
    abstract val outputFile: RegularFileProperty

    abstract val autoCreate: Property<Boolean>
    abstract val projectUUID: Property<String>
    abstract val projectName: Property<String>
    abstract val projectVersion: Property<String>
    abstract val projectTags: ListProperty<ProjectTag>
    abstract val parentUUID: Property<String>
    abstract val parentName: Property<String>
    abstract val parentVersion: Property<String>

    abstract val riskScoreData: Property<RiskScoreBuilder>

    abstract val vexComponentList: ListProperty<VexComponentBuilder>
    abstract val vexVulnerabilityList: ListProperty<VexVulnerabilityBuilder>
}

fun DepTrackCompanionExtension.vexComponent(action: VexComponentBuilder.() -> Unit) {
    vexComponentList.add(VexComponentBuilder(project).apply(action))
}

fun DepTrackCompanionExtension.vexVulnerability(action: VexVulnerabilityBuilder.() -> Unit) {
    vexVulnerabilityList.add(VexVulnerabilityBuilder(project).apply(action))
}

fun DepTrackCompanionExtension.riskScore(action: RiskScoreBuilder.() -> Unit) {
    riskScoreData.set(RiskScoreBuilder(project).apply(action))
}
