package com.liftric

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.liftric.extensions.*
import com.liftric.tasks.*

internal const val extensionName = "dependencyTrackCompanion"

class DepTrackHelperPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension =
            project.extensions.create(extensionName, DepTrackHelperExtension::class.java, project)

        extension.filePath.convention("reports/bom.json")
        extension.outputPath.convention("reports/")
        extension.outputFilename.convention("vex")

        project.tasks.register("generateVex", GenerateVexTask::class.java) { task ->
            task.url.set(extension.url)
            task.filePath.set(extension.filePath)
            task.outputPath.set(extension.outputPath)
            task.outputFilename.set(extension.outputFilename)
            task.vexComponent.set(extension.vexComponentList)
            task.vexVulnerability.set(extension.vexVulnerabilityList)
        }

        project.tasks.register("uploadSbom", UploadSBOMTask::class.java) { task ->
            task.url.set(extension.url)
            task.apiKey.set(extension.apiKey)
            task.filePath.set(extension.filePath)
            task.uploadSBOM.set(extension.uploadSBOMData)
        }

        project.tasks.register("uploadVex", UploadVexTask::class.java) { task ->
            task.outputPath.set(extension.outputPath)
            task.outputFilename.set(extension.outputFilename)
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.uploadVex.set(extension.uploadVexData)
        }

        project.tasks.register("getOutdatedDependencies", GetOutdatedDependenciesTask::class.java) { task ->
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.getOutdatedDependencies.set(extension.getOutdatedDependenciesData)
        }

        project.tasks.register("getSuppressedVuln", GetSuppressedVulnTask::class.java) { task ->
            task.apiKey.set(extension.apiKey)
            task.url.set(extension.url)
            task.getSuppressedVuln.set(extension.getSuppressedVulnData)
        }
    }
}

fun Project.dependencyTrackCompanion(): DepTrackHelperExtension {
    return extensions.getByName(extensionName) as? DepTrackHelperExtension
        ?: throw IllegalStateException("$extensionName is not of the correct type")
}
