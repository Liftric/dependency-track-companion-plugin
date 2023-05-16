package com.liftric

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.liftric.extensions.*
import com.liftric.tasks.*

internal const val extensionName = "dependencyTrackCompanion"
internal const val taskGroup = "Dependency Track Companion Plugin"

class DepTrackHelperPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension =
            project.extensions.create(extensionName, DepTrackHelperExtension::class.java, project)

        extension.filePath.convention("reports/bom.json")
        extension.outputPath.convention("reports/")
        extension.outputFilename.convention("vex")

        val uploadSbom = project.tasks.register("uploadSbom", UploadSBOMTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Uploads SBOM file"
            task.url.set(extension.url)
            task.apiKey.set(extension.apiKey)
            task.filePath.set(extension.filePath)
            task.uploadSBOM.set(extension.uploadSBOMData)
        }

        val generateVex = project.tasks.register("generateVex", GenerateVexTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Generates VEX file"
            task.filePath.set(extension.filePath)
            task.outputPath.set(extension.outputPath)
            task.outputFilename.set(extension.outputFilename)
            task.vexComponent.set(extension.vexComponentList)
            task.vexVulnerability.set(extension.vexVulnerabilityList)
            task.mustRunAfter(uploadSbom)
        }

        val uploadVex = project.tasks.register("uploadVex", UploadVexTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Uploads VEX file"
            task.outputPath.set(extension.outputPath)
            task.outputFilename.set(extension.outputFilename)
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.uploadVex.set(extension.uploadVexData)
            task.mustRunAfter(generateVex)
        }

        val getOutdatedDependencies = project.tasks.register("getOutdatedDependencies", GetOutdatedDependenciesTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Gets outdated dependencies"
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.getOutdatedDependencies.set(extension.getOutdatedDependenciesData)
            task.mustRunAfter(uploadVex)
        }

        val getSuppressedVuln = project.tasks.register("getSuppressedVuln", GetSuppressedVulnTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Gets suppressed vulnerabilities"
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.getSuppressedVuln.set(extension.getSuppressedVulnData)
            task.mustRunAfter(getOutdatedDependencies)
        }

        project.tasks.register("runDepTrackWorkflow") { task ->
            task.group = taskGroup
            task.description = "Runs all tasks to upload SBOM, generate VEX, upload VEX, get outdated dependencies and get suppressed vulnerabilities"
            task.dependsOn(uploadSbom, generateVex, uploadVex, getOutdatedDependencies, getSuppressedVuln)
        }
    }
}

fun Project.dependencyTrackCompanion(): DepTrackHelperExtension {
    return extensions.getByName(extensionName) as? DepTrackHelperExtension
        ?: throw IllegalStateException("$extensionName is not of the correct type")
}
