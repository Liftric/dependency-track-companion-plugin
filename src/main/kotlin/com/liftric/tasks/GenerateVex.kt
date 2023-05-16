package com.liftric.tasks

import org.cyclonedx.generators.json.BomJsonGenerator14
import org.cyclonedx.generators.xml.BomXmlGenerator14
import org.cyclonedx.model.Bom
import org.cyclonedx.model.vulnerability.Vulnerability
import org.cyclonedx.parsers.JsonParser
import org.cyclonedx.parsers.XmlParser
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import com.liftric.extensions.*
import org.cyclonedx.model.*
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import java.util.Date


abstract class GenerateVexTask : DefaultTask() {
    @get:Input
    abstract val filePath: Property<String>

    @get:Input
    abstract val outputPath: Property<String>

    @get:Input
    abstract val outputFilename: Property<String>

    @get:Nested
    abstract val vexComponent: ListProperty<VexComponentBuilder>

    @get:Nested
    abstract val vexVulnerability: ListProperty<VexVulnerabilityBuilder>

    private val vexFile = Bom()

    @TaskAction
    fun generateCyclonDXVex() {
        val filePathValue = filePath.get()
        val outputPathValue = outputPath.get()
        val outputFilenameValue = outputFilename.get()
        val outputFile = "$outputPathValue$outputFilenameValue"
        val vexComponentList = vexComponent.get().map { it.build() }
        val vexVulnerabilityList = vexVulnerability.get().map { it.build() }

        processCycloneDXReport(filePathValue, vexComponentList, vexVulnerabilityList)
        writeVexFile(outputFile)
        println("Generated VEX file")
    }

    private fun processCycloneDXReport(
        filePath: String,
        vexComponentList: List<VexComponent>,
        vexVulnerabilityList: List<VexVulnerability>,
    ) {
        val cycloneDxReportFile = project.buildDir.resolve(filePath)

        if (cycloneDxReportFile.exists()) {
            val sbom = handleFileFormat(cycloneDxReportFile, filePath)
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

    private fun handleFileFormat(file: File, filePath: String): Bom {
        return when (filePath.substringAfterLast(".")) {
            "json" -> parseJSONFile(file)
            "xml" -> parseXMLFile(file)
            else -> throw Exception("File format not supported")
        }
    }

    private fun parseJSONFile(file: File): Bom {
        return JsonParser().parse(file)
    }

    private fun parseXMLFile(file: File): Bom {
        return XmlParser().parse(file)
    }

    private fun writeVexFile(outputFile: String) {
        val xml = BomXmlGenerator14(vexFile).toXmlString()
        val xmlOutputPath = project.buildDir.resolve("${outputFile}.xml")
        Files.createDirectories(Paths.get(xmlOutputPath.parent))
        xmlOutputPath.writeText(xml)

        val json = BomJsonGenerator14(vexFile).toJsonString()
        val jsonOutputPath = project.buildDir.resolve("${outputFile}.json")
        Files.createDirectories(Paths.get(jsonOutputPath.parent))
        jsonOutputPath.writeText(json)
    }
}
