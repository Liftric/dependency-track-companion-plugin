package com.liftric.dtcp

import org.junit.jupiter.api.Assertions.assertNotNull
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test


class DepTrackCompanionPluginTest {
    @Test
    fun testApply() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.liftric.dependency-track-companion-plugin")
        assertNotNull(project.plugins.getPlugin(DepTrackCompanionPlugin::class.java))
    }

    @Test
    fun testExtension() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.liftric.dependency-track-companion-plugin")
        assertNotNull(project.dependencyTrackCompanion())
    }

    @Test
    fun testTasks() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.liftric.dependency-track-companion-plugin")
        assertNotNull(project.tasks.findByName("runDepTrackWorkflow"))
        assertNotNull(project.tasks.findByName("generateVex"))
        assertNotNull(project.tasks.findByName("uploadSbom"))
        assertNotNull(project.tasks.findByName("uploadVex"))
        assertNotNull(project.tasks.findByName("riskScore"))
        assertNotNull(project.tasks.findByName("getOutdatedDependencies"))
        assertNotNull(project.tasks.findByName("getSuppressedVuln"))
    }
}
