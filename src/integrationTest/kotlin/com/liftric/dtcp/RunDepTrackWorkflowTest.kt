package com.liftric.dtcp

import com.liftric.dtcp.model.VexComponent
import com.liftric.dtcp.model.VexVulnerability
import com.liftric.dtcp.service.ApiService
import com.liftric.dtcp.service.IgnoreErrorApiService
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.runBlocking

/**
 * In this test, we are simulating the process of creating a project and running Dependency Track workflow.
 * Initially, we retrieve the access key using ApiService. Due to the peculiar behavior of Dependency Track
 * when run with Docker Compose (authentication disabled), we use ApiServiceIgnoreError to create the project,
 * as it is set to ignore the error (HTTP status 500) returned by the API.
 *
 * For more information about why this is necessary, refer to the ApiServiceIgnoreError class comments.
 */

class RunDepTrackWorkflowTest: IntegrationTestBase() {
    @Test
    fun testRunDepTrackWorkflowTest() {
        val projectName = "dtTest"
        val version = "1.0.0"

        val dependencyTrackAccessKey =
            runBlocking { ApiService(dependencyTrackApiEndpoint).getDependencyTrackAccessKey() }

        assertTrue(dependencyTrackAccessKey.isNotEmpty())

        runBlocking {
            IgnoreErrorApiService(
                dependencyTrackApiEndpoint,
                dependencyTrackAccessKey
            ).createProject(projectName)
        }

        val projectDir = File("build/runDepTrackWorkflowTest")

        projectDir.mkdirs()
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText(
            """
import com.liftric.dtcp.extensions.*
import org.cyclonedx.model.vulnerability.Vulnerability

plugins {
    kotlin("jvm") version "1.8.21"
    id("com.liftric.dependency-track-companion-plugin")
}

repositories {
    mavenCentral()
}

group = "com.liftric.$projectName"
version = "$version"

dependencyTrackCompanion {
    url.set("$dependencyTrackApiEndpoint")
    apiKey.set("$dependencyTrackAccessKey")
    projectName.set("$projectName")
    projectVersion.set("$version")
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

        val result = GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withArguments("build", "runDepTrackWorkflow")
            .withPluginClasspath().build()

        println(result.output)
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }
}
