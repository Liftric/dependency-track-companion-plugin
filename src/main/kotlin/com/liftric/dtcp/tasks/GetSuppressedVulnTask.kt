package com.liftric.dtcp.tasks

import com.liftric.dtcp.model.Finding
import com.liftric.dtcp.service.DependencyTrack
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class GetSuppressedVulnTask : DefaultTask() {
    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val url: Property<String>

    @get:Input
    @get:Optional
    abstract val projectUUID: Property<String>

    @get:Input
    @get:Optional
    abstract val projectName: Property<String>

    @get:Input
    @get:Optional
    abstract val projectVersion: Property<String>

    @TaskAction
    fun getSuppressedVulnTask() {
        val apiKeyValue = apiKey.get()
        val urlValue = url.get()
        val projectUUIDValue = projectUUID.orNull
        val projectNameValue = projectName.orNull
        val projectVersionValue = projectVersion.orNull

        val dt = DependencyTrack(apiKeyValue, urlValue)

        val findings = if (projectUUIDValue != null) {
            dt.getProjectFindingsById(projectUUIDValue)
        } else if (projectNameValue != null && projectVersionValue != null) {
            val project = dt.getProject(projectNameValue, projectVersionValue)
            dt.getProjectFindingsById(project.uuid)
        } else {
            throw Exception("Either projectUUID or projectName and projectVersion must be set")
        }

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
