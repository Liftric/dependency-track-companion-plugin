package com.liftric.dtcp

import com.liftric.dtcp.model.VexComponent
import com.liftric.dtcp.model.VexVulnerability
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import org.cyclonedx.parsers.JsonParser

class GenerateVexTest: IntegrationTestBase() {
    @Test
    fun testGenerateVexTask() {
        val projectDir = File("build/generateVexTest")

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

group = "com.liftric.test"
version = "1.0.0"

dependencyTrackCompanion {
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
            .withArguments("build", "generateVex")
            .withPluginClasspath().build()

        val generatedVexFile = projectDir.resolve("build/reports/vex.json")
        assertTrue(generatedVexFile.exists())
        assertTrue(JsonParser().isValid(generatedVexFile))

        val generatedVexBom = JsonParser().parse(generatedVexFile)
        testVexBomContainsVulnerability(generatedVexBom, vexVulnerability)
        testVexBomContainsComponent(generatedVexBom, vexComponent)

        println(result.output)
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    private fun testVexBomContainsVulnerability(
        generatedVexBom: org.cyclonedx.model.Bom,
        vexVulnerability: VexVulnerability,
    ) {
        val vulnerability = generatedVexBom.vulnerabilities.find { it.id == vexVulnerability.id }
        assertNotNull(vulnerability)

        vulnerability?.let {
            assertEquals(it.id, vexVulnerability.id)
            assertEquals(it.source.name, vexVulnerability.source)
            assertEquals(it.analysis.state.name, vexVulnerability.analysisValue)
            assertEquals(it.analysis.detail, vexVulnerability.detail)
        }
    }

    private fun testVexBomContainsComponent(
        generatedVexBom: org.cyclonedx.model.Bom,
        vexComponent: VexComponent,
    ) {
        val component = generatedVexBom.components.find { it.purl == vexComponent.purl }
        assertNotNull(component)

        component?.let {
            assertEquals(it.purl, vexComponent.purl)
            testVexBomContainsVulnerability(generatedVexBom, vexComponent.vulnerability)
        }
    }
}
