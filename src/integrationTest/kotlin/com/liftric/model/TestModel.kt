package com.liftric.model

import kotlinx.serialization.Serializable

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

@Serializable
data class Team(
    val uuid: String,
    val name: String,
)

@Serializable
data class KeyResponse(
    val key: String,
)


@Serializable
data class ProjectData(
    val author: String,
    val publisher: String,
    val group: String,
    val name: String,
    val description: String,
    val version: String,
    val classifier: String,
    val active: Boolean
)
