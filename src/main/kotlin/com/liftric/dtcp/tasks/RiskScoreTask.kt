package com.liftric.dtcp.tasks

import com.liftric.dtcp.extensions.RiskScoreBuilder
import com.liftric.dtcp.service.DependencyTrack
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

abstract class RiskScoreTask : DefaultTask() {
    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val url: Property<String>

    @get:Nested
    @get:Optional
    abstract val riskScore: Property<RiskScoreBuilder>

    @OptIn(ExperimentalTime::class)
    @TaskAction
    fun riskScoreTask() {
        val apiKeyValue = apiKey.get()
        val urlValue = url.get()
        val riskScoreValue = riskScore.orNull
        if (riskScoreValue == null) {
            logger.info("Skipping risk score calculation")
            return
        }
        val projectName = riskScoreValue.projectName.get()
        val projectVersion = riskScoreValue.projectVersion.get()
        val maxRiskScore = riskScoreValue.maxRiskScore.orNull
        val timeout = riskScoreValue.timeout.getOrElse(Duration.ZERO)

        val dt = DependencyTrack(apiKeyValue, urlValue)
        val project = dt.getProject(projectName, projectVersion)
        dt.analyzeProjectFindings(project.uuid)
        logger.info("Reanalyse triggered, waiting $timeout for analysis to finish")
        runBlocking {
            delay(timeout)
        }

        val updatedProject = dt.getProject(projectName, projectVersion)
        logger.info("Risk Score: ${updatedProject.lastInheritedRiskScore}")

        if (maxRiskScore != null && maxRiskScore < updatedProject.lastInheritedRiskScore!!) {
            throw GradleException("Risk score of ${updatedProject.lastInheritedRiskScore} exceeds maxRiskScore of $maxRiskScore")
        }
    }
}
