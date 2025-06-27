package com.liftric.dtcp

import com.liftric.dtcp.extensions.DepTrackCompanionExtension
import com.liftric.dtcp.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project

internal const val extensionName = "dependencyTrackCompanion"
internal const val taskGroup = "Dependency Track Companion Plugin"

class DepTrackCompanionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("org.cyclonedx.bom")
        val extension =
            project.extensions.create(extensionName, DepTrackCompanionExtension::class.java, project)

        extension.inputFile.convention(
            project.layout.buildDirectory.file("reports/bom.json")
        )
        extension.outputFile.convention(
            project.layout.buildDirectory.file("reports/vex.json")
        )
        extension.autoCreate.convention(false)

        val createProject = project.tasks.register("createProject", CreateProject::class.java) { task ->
            task.group = taskGroup
            task.description = "Creates a project"
            task.url.set(extension.url)
            task.apiKey.set(extension.apiKey)
            task.projectActive.set(extension.projectActive)
            task.projectTags.set(extension.projectTags)
            task.projectName.set(extension.projectName)
            task.projectVersion.set(extension.projectVersion)
            task.parentUUID.set(extension.parentUUID)
            task.ignoreProjectAlreadyExists.set(extension.ignoreProjectAlreadyExists)
        }

        val generateSbom = project.tasks.register("generateSbom") { task ->
            task.group = taskGroup
            task.description = "Generate SBOM file"
            task.dependsOn("cyclonedxBom")
        }

        val uploadSbom = project.tasks.register("uploadSbom", UploadSBOMTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Uploads SBOM file"
            task.url.set(extension.url)
            task.apiKey.set(extension.apiKey)
            task.inputFile.set(extension.inputFile)
            task.autoCreate.set(extension.autoCreate)
            task.projectUUID.set(extension.projectUUID)
            task.projectName.set(extension.projectName)
            task.projectVersion.set(extension.projectVersion)
            task.parentUUID.set(extension.parentUUID)
            task.parentName.set(extension.parentName)
            task.parentVersion.set(extension.parentVersion)
            task.ignoreErrors.set(extension.ignoreErrors)
            task.dependsOn(generateSbom)
        }

        val generateVex = project.tasks.register("generateVex", GenerateVexTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Generates VEX file"
            task.inputFile.set(extension.inputFile)
            task.outputFile.set(extension.outputFile)
            task.vexComponent.set(extension.vexComponentList)
            task.vexVulnerability.set(extension.vexVulnerabilityList)
            task.mustRunAfter(uploadSbom)
            task.dependsOn(generateSbom)
        }

        val uploadVex = project.tasks.register("uploadVex", UploadVexTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Uploads VEX file"
            task.outputFile.set(extension.outputFile)
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.projectUUID.set(extension.projectUUID)
            task.projectName.set(extension.projectName)
            task.projectVersion.set(extension.projectVersion)
            task.mustRunAfter(generateVex)
            task.dependsOn(generateVex)
        }

        val analyzeProject = project.tasks.register("analyzeProject", AnalyzeProjectTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Triggers Vulnerability Analysis on a specific project\n"
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.projectUUID.set(extension.projectUUID)
            task.projectName.set(extension.projectName)
            task.projectVersion.set(extension.projectVersion)
        }

        val riskScore = project.tasks.register("riskScore", RiskScoreTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Get Risk Score"
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.projectUUID.set(extension.projectUUID)
            task.projectName.set(extension.projectName)
            task.projectVersion.set(extension.projectVersion)
            task.riskScore.set(extension.riskScoreData)
        }

        project.tasks.register("runDepTrackWorkflow") { task ->
            task.group = taskGroup
            task.description =
                "Runs generateSbom, uploadSbom, generateVex, uploadVex for CI/CD integration"
            task.dependsOn(generateSbom, uploadSbom, generateVex, uploadVex)
        }

        project.tasks.register("getOutdatedDependencies", GetOutdatedDependenciesTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Gets outdated dependencies"
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.projectUUID.set(extension.projectUUID)
            task.projectName.set(extension.projectName)
            task.projectVersion.set(extension.projectVersion)
        }

        project.tasks.register("getSuppressedVuln", GetSuppressedVulnTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Gets suppressed vulnerabilities"
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.projectUUID.set(extension.projectUUID)
            task.projectName.set(extension.projectName)
            task.projectVersion.set(extension.projectVersion)
        }
    }
}

fun Project.dependencyTrackCompanion(): DepTrackCompanionExtension {
    return extensions.getByName(extensionName) as? DepTrackCompanionExtension
        ?: throw IllegalStateException("$extensionName is not of the correct type")
}
