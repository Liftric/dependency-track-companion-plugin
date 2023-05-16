package com.liftric

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

data class VexComponent(
    val purl: String,
    val vulnerability: VexVulnerability,
)

data class VexVulnerability(
    val id: String,
    val source: String,
    val analysis: String,
    val analysisValue: String,
    val detail: String?,
)

class PluginIntegrationTest {

    @Test
    fun testGenerateVexTask() {

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


        val projectDir = File("build/generateVexTest")

        projectDir.mkdirs()
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText(
            """
import com.liftric.extensions.*
import org.cyclonedx.model.vulnerability.Vulnerability

plugins {
    id("com.liftric.dependency-track-companion-plugin")
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

        val sourceJsonFile = Paths.get("test/data/bom.json")
        val targetJsonFile = projectDir.toPath().resolve("build/reports/bom.json")
        Files.createDirectories(targetJsonFile.parent)
        Files.copy(sourceJsonFile, targetJsonFile, StandardCopyOption.REPLACE_EXISTING)

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
