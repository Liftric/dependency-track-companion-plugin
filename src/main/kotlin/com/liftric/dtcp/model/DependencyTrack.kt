package com.liftric.dtcp.model

import kotlinx.serialization.Serializable

@Serializable
data class Component(
    val group: String,
    val name: String,
    val version: String,
    val purl: String,
    val uuid: String,
    val repositoryMeta: RepositoryMeta? = null,
)

@Serializable
data class RepositoryMeta(
    val latestVersion: String,
)

@Serializable
data class Project(
    val uuid: String,
    val name: String,
    val version: String,
    val active: Boolean,
    val classifier: String,
    val directDependencies: String? = null,
    val lastInheritedRiskScore: Double? = null,
)

@Serializable
data class CreateProject(
    val name: String,
    val version: String? = null,
    val active: Boolean,
    val tags: List<ProjectTag>,
    val parent: Parent? = null,
) {
    @Serializable
    data class Parent(
        val uuid: String? = null,
    )
}

@Serializable
data class ProjectTag(
    val name: String,
)

@Serializable
data class DirectDependency(
    val name: String,
    val purl: String,
    val uuid: String,
    val version: String,
    val group: String,
)

@Serializable
data class Finding(
    val component: FindingComponent,
    val vulnerability: Vulnerability,
    val analysis: Analysis,
)

@Serializable
data class FindingComponent(
    val uuid: String,
    val name: String,
    val group: String,
    val version: String,
    val purl: String,
    val project: String,
)

@Serializable
data class Vulnerability(
    val uuid: String,
    val source: String,
    val vulnId: String,
)

@Serializable
data class Analysis(
    val state: String? = null,
    val isSuppressed: Boolean,
)

@Serializable
data class TaskTokenResponse(val token: String)

@Serializable
data class EventTokenResponse(val processing: Boolean)