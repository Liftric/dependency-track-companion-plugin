package com.liftric.dtcp.tasks

import com.liftric.dtcp.extensions.VexComponent
import com.liftric.dtcp.extensions.VexComponentBuilder
import com.liftric.dtcp.extensions.VexVulnerability
import com.liftric.dtcp.extensions.VexVulnerabilityBuilder
import org.cyclonedx.generators.json.BomJsonGenerator14
import org.cyclonedx.model.Bom
import org.cyclonedx.model.vulnerability.Vulnerability
import org.cyclonedx.parsers.JsonParser
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import org.cyclonedx.model.*
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import java.util.Date


abstract class GenerateVexTask : DefaultTask() {
    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Nested
    abstract val vexComponent: ListProperty<VexComponentBuilder>

    @get:Nested
    abstract val vexVulnerability: ListProperty<VexVulnerabilityBuilder>

    private val vexFile = Bom()

    @TaskAction
    fun generateCyclonDXVex() {
        val inputFileValue = inputFile.get().asFile
        val outputFileValue = outputFile.get().asFile
        val vexComponentList = vexComponent.get().map { it.build() }
        val vexVulnerabilityList = vexVulnerability.get().map { it.build() }

        processCycloneDXReport(inputFileValue, vexComponentList, vexVulnerabilityList)
        writeVexFile(outputFileValue)
        println("Generated VEX file")
    }

    private fun processCycloneDXReport(
        file: File,
        vexComponentList: List<VexComponent>,
        vexVulnerabilityList: List<VexVulnerability>,
    ) {
        if (file.exists()) {
            val sbom = parseInputFile(file)
            handleBom(sbom, vexComponentList, vexVulnerabilityList)
        } else {
            throw Exception("CycloneDX report file not found, run './gradlew cyclonedxBom'")
        }
    }

    private fun handleBom(
        sbom: Bom,
        vexComponentList: List<VexComponent>,
        vexVulnerabilityList: List<VexVulnerability>,
    ) {
        handleMetadata(sbom)
        handleComponents(sbom, vexComponentList)
        handleVulnerabilities(sbom, vexVulnerabilityList)
    }

    private fun handleVulnerabilities(sbom: Bom, vexVulnerabilityList: List<VexVulnerability>) {
        vexVulnerabilityList.forEach { vexVulnerability ->
            addVulnerability(sbom.metadata.component.bomRef, vexVulnerability)
        }
    }

    private fun handleMetadata(sbom: Bom) {
        vexFile.metadata = Metadata()
        vexFile.metadata.timestamp = Date()
        vexFile.metadata.component = sbom.metadata.component ?: Component()
        val pluginData = Tool().apply {
            vendor = "TODO"
            name = "TODO"
            version = "TODO"
        }
        vexFile.metadata.tools = listOf(pluginData)
    }

    private fun handleComponents(sbom: Bom, vexComponentList: List<VexComponent>) {
        sbom.components.forEach { component ->
            vexComponentList.forEach { vexComponent ->
                if (component.purl == vexComponent.purl) {
                    vexFile.addComponent(component)
                    addVulnerability(vexComponent.purl, vexComponent.vulnerability)
                }
            }
        }
    }

    private fun addVulnerability(purl: String, vexVulnerability: VexVulnerability) {
        val vulnerability = Vulnerability()
        vulnerability.id = vexVulnerability.id
        vulnerability.source = Vulnerability.Source().apply {
            name = vexVulnerability.source
        }
        vulnerability.analysis = Vulnerability.Analysis().apply {
            state = vexVulnerability.analysis
            detail = vexVulnerability.detail
        }
        vulnerability.affects = listOf(Vulnerability.Affect().apply {
            ref = purl
        })
        vexFile.vulnerabilities = (vexFile.vulnerabilities ?: emptyList()) + vulnerability
    }

    private fun parseInputFile(file: File): Bom = JsonParser().parse(file)

    private fun writeVexFile(outputFile: File) {
        val json = BomJsonGenerator14(vexFile).toJsonString()
        Files.createDirectories(Paths.get(outputFile.parent))
        outputFile.writeText(json)
    }
}
