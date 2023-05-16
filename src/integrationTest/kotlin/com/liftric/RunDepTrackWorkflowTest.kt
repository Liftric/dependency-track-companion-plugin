package com.liftric

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import com.liftric.model.*
import com.liftric.service.*
import kotlinx.coroutines.runBlocking

/**
 * In this test, we are simulating the process of creating a project and running Dependency Track workflow.
 * Initially, we retrieve the access key using ApiService. Due to the peculiar behavior of Dependency Track
 * when run with Docker Compose (authentication disabled), we use ApiServiceIgnoreError to create the project,
 * as it is set to ignore the error (HTTP status 500) returned by the API.
 *
 * For more information about why this is necessary, refer to the ApiServiceIgnoreError class comments.
 */

internal const val dependencyTrackApiEndpoint = "http://localhost:8081"

class RunDepTrackWorkflowTest {
    @Test
    fun testRunDepTrackWorkflowTest() {
        val projectName = "dtTest"
        val version = "1.0.0"

        val dependencyTrackAccessKey =
            runBlocking { ApiService(dependencyTrackApiEndpoint).getDependencyTrackAccessKey() }

        assertTrue(dependencyTrackAccessKey.isNotEmpty())

        runBlocking {
            ApiServiceIgnoreError(
                dependencyTrackApiEndpoint,
                dependencyTrackAccessKey
            ).createProject(projectName)
        }

        val vexComponent = VexComponent(
            purl = "pkg:maven/org.eclipse.jetty/jetty-http@9.4.49.v20220914?type=jar",
            vulnerability = VexVulnerability(
                id = "CVE-2023-26048",
                source = "NVD",
                analysis = "Vulnerability.Analysis.State.FALSE_POSITIVE",
                analysisValue = "FALSE_POSITIVE",
                detail = null,
            )
        )

        val vexVulnerability = VexVulnerability(
            id = "CVE-2020-8908",
            source = "NVD",
            analysis = "Vulnerability.Analysis.State.RESOLVED",
            analysisValue = "RESOLVED",
            detail = "This is resolved",
        )


        val projectDir = File("build/runDepTrackWorkflowTest")

        projectDir.mkdirs()
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText(
            """
import com.liftric.extensions.*
import org.cyclonedx.model.vulnerability.Vulnerability

plugins {
    id("com.liftric.dependency-track-companion-plugin")
}

group = "com.liftric.$projectName"
version = "$version"

dependencyTrackCompanion {
    url.set("$dependencyTrackApiEndpoint")
    apiKey.set("$dependencyTrackAccessKey")
    uploadSBOM {
        projectName.set("$projectName")
        projectVersion.set("$version")
    }
    uploadVex {
        projectName.set("$projectName")
        projectVersion.set("$version")
    }
    getOutdatedDependencies {
        projectName.set("$projectName")
        projectVersion.set("$version")
    }
    getSuppressedVuln {
        projectName.set("$projectName")
        projectVersion.set("$version")
    }
    vexComponent {
        purl.set("${vexComponent.purl}")
        vulnerability {
            id.set("${vexComponent.vulnerability.id}")
            source.set("${vexComponent.vulnerability.source}")
            analysis.set(${vexComponent.vulnerability.analysis})
        }
    }
    vexVulnerability {
        id.set("${vexVulnerability.id}")
        source.set("${vexVulnerability.source}")
        analysis.set(${vexVulnerability.analysis})
        detail.set("${vexVulnerability.detail}")
    }
}
        """
        )

        val sourceJsonFile = Paths.get("test/data/bom.json")
        val targetJsonFile = projectDir.toPath().resolve("build/reports/bom.json")
        Files.createDirectories(targetJsonFile.parent)
        Files.copy(sourceJsonFile, targetJsonFile, StandardCopyOption.REPLACE_EXISTING)

        val result = GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withArguments("build", "runDepTrackWorkflow")
            .withPluginClasspath().build()

        println(result.output)
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }
}
