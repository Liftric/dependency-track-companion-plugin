package com.liftric.dtcp.extensions

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class DepTrackCompanionExtension(val project: Project) {
    abstract val url: Property<String>
    abstract val apiKey: Property<String>

    abstract val inputFile: RegularFileProperty
    abstract val outputFile: RegularFileProperty

    abstract val uploadSBOMData: Property<UploadSBOMBuilder>

    abstract val uploadVexData: Property<UploadVexBuilder>
    abstract val getOutdatedDependenciesData: Property<GetOutdatedDependenciesBuilder>
    abstract val getSuppressedVulnData: Property<GetSuppressedVulnBuilder>

    abstract val vexComponentList: ListProperty<VexComponentBuilder>
    abstract val vexVulnerabilityList: ListProperty<VexVulnerabilityBuilder>
}

fun DepTrackCompanionExtension.vexComponent(action: VexComponentBuilder.() -> Unit) {
    vexComponentList.add(VexComponentBuilder(project).apply(action))
}


fun DepTrackCompanionExtension.vexVulnerability(action: VexVulnerabilityBuilder.() -> Unit) {
    vexVulnerabilityList.add(VexVulnerabilityBuilder(project).apply(action))
}

fun DepTrackCompanionExtension.uploadSBOM(action: UploadSBOMBuilder.() -> Unit) {
    uploadSBOMData.set(UploadSBOMBuilder(project).apply(action))
}


fun DepTrackCompanionExtension.uploadVex(action: UploadVexBuilder.() -> Unit) {
    uploadVexData.set(UploadVexBuilder(project).apply(action))
}

fun DepTrackCompanionExtension.getOutdatedDependencies(action: GetOutdatedDependenciesBuilder.() -> Unit) {
    getOutdatedDependenciesData.set(GetOutdatedDependenciesBuilder(project).apply(action))
}

fun DepTrackCompanionExtension.getSuppressedVuln(action: GetSuppressedVulnBuilder.() -> Unit) {
    getSuppressedVulnData.set(GetSuppressedVulnBuilder(project).apply(action))
}
