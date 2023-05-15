package com.liftric.tasks

import com.liftric.service.*
import com.liftric.extensions.*
import com.liftric.model.*
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

abstract class GetSuppressedVulnTask : DefaultTask() {
    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val url: Property<String>

    @get:Nested
    abstract val getSuppressedVuln: Property<GetSuppressedVulnBuilder>

    @TaskAction
    fun getSuppressedVulnTask() {
        val apiKeyValue = apiKey.get()
        val urlValue = url.get()
        val inputData = getSuppressedVuln.get().build()

        val dt = DependencyTrack(apiKeyValue, urlValue)
        val project = dt.getProject(inputData.projectName, inputData.projectVersion)
        val findings = dt.getProjectFindingsById(project.uuid)

        printSuppressedVuln(findings)
    }

    private fun printSuppressedVuln(findings: List<Finding>) {
        findings.forEach { finding ->
            if (finding.analysis.isSuppressed) {
                println(
                    """
                    |vexComponent {
                    |    purl = "${finding.component.purl}"
                    |    vulnerability {
                    |        id = "${finding.vulnerability.vulnId}"
                    |        source = "${finding.vulnerability.source}"
                    |        analysis = Vulnerability.Analysis.State.${finding.analysis.state}
                    |    }
                    |}
                    """.trimMargin()
                )
            }
        }
    }
}
