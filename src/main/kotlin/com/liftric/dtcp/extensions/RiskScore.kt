package com.liftric.dtcp.extensions

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

data class RiskScore @OptIn(ExperimentalTime::class) constructor(
    val projectName: String,
    val projectVersion: String,
    val maxRiskScore: Double?,
    val timeout: Duration?,
)

@Suppress("MemberVisibilityCanBePrivate")
@ConfigDsl
class RiskScoreBuilder(@get:Internal val proj: Project) {
    @get:Input
    @get:Optional
    val projectName: Property<String> = proj.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val projectVersion: Property<String> = proj.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val maxRiskScore: Property<Double> = proj.objects.property(Double::class.java)

    @OptIn(ExperimentalTime::class)
    @get:Input
    @get:Optional
    val timeout: Property<Duration> = proj.objects.property(Duration::class.java)

    @OptIn(ExperimentalTime::class)
    fun build(): RiskScore = RiskScore(
        projectName = this.projectName.get(),
        projectVersion = this.projectVersion.get(),
        maxRiskScore = this.maxRiskScore.orNull,
        timeout = this.timeout.orNull,
    )
}
