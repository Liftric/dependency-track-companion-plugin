package com.liftric.dtcp

import com.liftric.dtcp.service.ApiService
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import kotlinx.coroutines.runBlocking
import org.gradle.testkit.runner.UnexpectedBuildFailure

class CreateProjectTest : IntegrationTestBase() {
    @Test
    fun testCreateProjectTest() {
        val projectName = "createProjectTest"
        val version = "1.0.0"

        val apiService = ApiService(dependencyTrackApiEndpoint)

        val dependencyTrackAccessKey =
            runBlocking { apiService.getDependencyTrackAccessKey() }

        assertTrue(dependencyTrackAccessKey.isNotEmpty())

        val projectDir = File("build/createProjectTest")

        projectDir.mkdirs()
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText(
            """
import com.liftric.dtcp.extensions.*

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
    projectActive.set(false)
}
        """
        )

        /**
         * GradleRunner fails under the hood, but the Project is created successfully.
         * see [IgnoreErrorApiService] for more info.
         * */
        try {
            GradleRunner
                .create()
                .withProjectDir(projectDir)
                .withArguments("build", "createProject")
                .withPluginClasspath().build()
        } catch (e: UnexpectedBuildFailure) {
            assertTrue(e.message!!.contains("/api/v1/project: 500 Server Error"))
        }

        runBlocking {
            assertTrue(apiService.verifyProjectCreation(dependencyTrackAccessKey, projectName, version))
        }
    }
}
