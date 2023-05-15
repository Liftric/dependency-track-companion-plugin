//package com.liftric
//
//import org.junit.jupiter.api.Assertions.assertNotNull
//import org.gradle.testfixtures.ProjectBuilder
//import org.junit.jupiter.api.Test
//
//
//class DepTrackerHelperPluginTest {
//    @Test
//    fun testApply() {
//        val project = ProjectBuilder.builder().build()
//        project.pluginManager.apply("com.liftric.dep-track-helper-plugin")
//        assertNotNull(project.plugins.getPlugin(DepTrackHelperPlugin::class.java))
//    }
//
//    @Test
//    fun testExtension() {
//        val project = ProjectBuilder.builder().build()
//        project.pluginManager.apply("com.liftric.dep-track-helper-plugin")
//        assertNotNull(project.DepTrackHelperPlugin())
//    }
//
//    @Test
//    fun testTasks() {
//        val project = ProjectBuilder.builder().build()
//        project.pluginManager.apply("com.liftric.dep-track-helper-plugin")
////        assertNotNull(project.tasks.findByName("firstCommitHash"))
////        assertNotNull(project.tasks.findByName("previousTag"))
////        assertNotNull(project.tasks.findByName("commitsSinceLastTag"))
////        assertNotNull(project.tasks.findByName("createBuildInformation"))
////        assertNotNull(project.tasks.findByName("uploadBuildInformation"))
////        assertNotNull(project.tasks.findByName("uploadPackage"))
//    }
//}
