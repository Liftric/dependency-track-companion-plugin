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

    @get:Input
    @get:Optional
    abstract val projectUUID: Property<String>

    @get:Input
    @get:Optional
    abstract val projectName: Property<String>

    @get:Input
    @get:Optional
    abstract val projectVersion: Property<String>

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

        val projectUUIDValue = projectUUID.orNull
        val projectNameValue = projectName.orNull
        val projectVersionValue = projectVersion.orNull

        val maxRiskScore = riskScoreValue.maxRiskScore.orNull
        val timeout = riskScoreValue.timeout.getOrElse(Duration.ZERO)

        val dt = DependencyTrack(apiKeyValue, urlValue)

        val uuid = when {
            projectUUIDValue != null -> projectUUIDValue
            projectNameValue != null && projectVersionValue != null -> dt.getProject(
                projectNameValue,
                projectVersionValue
            ).uuid

            else -> throw GradleException("Either projectUUID or projectName and projectVersion must be set")
        }

        dt.analyzeProjectFindings(uuid)
        logger.info("Reanalyse triggered, waiting $timeout for analysis to finish")
        runBlocking {
            delay(timeout)
        }

        val updatedProject = dt.getProject(uuid)
        logger.info("Risk Score: ${updatedProject.lastInheritedRiskScore}")

        if (updatedProject.lastInheritedRiskScore == null) {
            throw GradleException("Risk score is null")
        }

        if (maxRiskScore != null && maxRiskScore < updatedProject.lastInheritedRiskScore) {
            throw GradleException("Risk score of ${updatedProject.lastInheritedRiskScore} exceeds maxRiskScore of $maxRiskScore")
        }
    }
}
