package com.liftric.dtcp

import com.liftric.dtcp.extensions.DepTrackCompanionExtension
import com.liftric.dtcp.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project

internal const val extensionName = "dependencyTrackCompanion"
internal const val taskGroup = "Dependency Track Companion Plugin"

class DepTrackCompanionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension =
            project.extensions.create(extensionName, DepTrackCompanionExtension::class.java, project)

        extension.inputFile.convention(
            project.layout.buildDirectory.file("reports/bom.json")
        )
        extension.outputFile.convention(
            project.layout.buildDirectory.file("reports/vex.json")
        )

        val uploadSbom = project.tasks.register("uploadSbom", UploadSBOMTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Uploads SBOM file"
            task.url.set(extension.url)
            task.apiKey.set(extension.apiKey)
            task.inputFile.set(extension.inputFile)
            task.uploadSBOM.set(extension.uploadSBOMData)
        }

        val generateVex = project.tasks.register("generateVex", GenerateVexTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Generates VEX file"
            task.inputFile.set(extension.inputFile)
            task.outputFile.set(extension.outputFile)
            task.vexComponent.set(extension.vexComponentList)
            task.vexVulnerability.set(extension.vexVulnerabilityList)
            task.mustRunAfter(uploadSbom)
        }

        val uploadVex = project.tasks.register("uploadVex", UploadVexTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Uploads VEX file"
            task.outputFile.set(extension.outputFile)
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.uploadVex.set(extension.uploadVexData)
            task.mustRunAfter(generateVex)
        }

        project.tasks.register("runDepTrackWorkflow") { task ->
            task.group = taskGroup
            task.description =
                "Runs uploadSbom, generateVex and uploadVex for CI/CD"
            task.dependsOn(uploadSbom, generateVex, uploadVex)
        }

        project.tasks.register("getOutdatedDependencies", GetOutdatedDependenciesTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Gets outdated dependencies"
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.getOutdatedDependencies.set(extension.getOutdatedDependenciesData)
        }

        project.tasks.register("getSuppressedVuln", GetSuppressedVulnTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Gets suppressed vulnerabilities"
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.getSuppressedVuln.set(extension.getSuppressedVulnData)
        }
    }
}

fun Project.dependencyTrackCompanion(): DepTrackCompanionExtension {
    return extensions.getByName(extensionName) as? DepTrackCompanionExtension
        ?: throw IllegalStateException("$extensionName is not of the correct type")
}
