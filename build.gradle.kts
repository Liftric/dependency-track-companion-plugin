@Suppress("DSL_SCOPE_VIOLATION") // IntelliJ incorrectly marks libs as not callable
plugins {
    `maven-publish`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.dockerCompose)
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.versioning)
}

group = "com.liftric"
version = with(versioning.info) {
    if (branch == "HEAD" && dirty.not()) tag else full
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dockerCompose {
    // Docker Compose v1 is needed for this Plugin to work
    useComposeFiles.set(listOf("docker-compose.yml"))
    waitForTcpPorts.set(true)
    captureContainersOutput.set(true)
    stopContainers.set(true)
    removeContainers.set(true)
    buildBeforeUp.set(true)
}

val integrationTest = sourceSets.create("integrationTest")

tasks {
    val test by existing
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        systemProperty("org.gradle.testkit.dir", gradle.gradleUserHomeDir)
    }

    val integrationTestTask = register<Test>("integrationTest") {
        description = "Runs the integration tests"
        group = "verification"
        testClassesDirs = integrationTest.output.classesDirs
        classpath = integrationTest.runtimeClasspath
        mustRunAfter(test)
        useJUnitPlatform()
    }
    dockerCompose.isRequiredBy(integrationTestTask)

    val pluginPropertiesBuildFolder = file("$buildDir/compileProperties/")
    val propertiesTask = register<WriteProperties>("writePluginProperties") {
        outputFile = pluginPropertiesBuildFolder.resolve("plugin.properties")
        property("vendor", "Liftric")
        property("name", rootProject.name)
        property("version", rootProject.version)
    }
    sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).resources.srcDir(pluginPropertiesBuildFolder)

    withType(ProcessResources::class.java) {
        dependsOn(propertiesTask)
    }
}

tasks.named("publishPlugins") {
    dependsOn("writePluginProperties")
}

gradlePlugin {
    website.set("https://github.com/Liftric/dependency-track-companion-plugin")
    vcsUrl.set("https://github.com/Liftric/dependency-track-companion-plugin")
    testSourceSets(integrationTest)
    plugins {
        create("dependency-track-companion-plugin") {
            id = "$group.$name"
            implementationClass = "$group.dtcp.DepTrackCompanionPlugin"
            displayName = name
            description = "Common tasks for Dependency Track interaction, like SBOM upload or VEX Generation"
            tags.set(listOf("dependency", "track", "sbom", "vex", "upload", "generate"))
        }
    }
}

dependencies {
    implementation(platform(libs.kotlinBom))
    implementation(libs.kotlinStdlibJdk8)
    implementation(libs.cyclonedxCoreJava)
    implementation(libs.ktorClientCio)
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientJson)
    implementation(libs.ktorClientSerialization)
    implementation(libs.ktorClientContentNegotiation)
    implementation(libs.ktorSerializationKotlinxJson)
    implementation(libs.kotlinReflect)
    implementation(libs.cyclonedxGradlePlugin)

    testImplementation(libs.junitJupiter)

    "integrationTestImplementation"(gradleTestKit())
    "integrationTestImplementation"(libs.cyclonedxCoreJava)
    "integrationTestImplementation"(libs.junitJupiter)
    "integrationTestImplementation"(libs.ktorClientCio)
    "integrationTestImplementation"(libs.ktorClientContentNegotiation)
    "integrationTestImplementation"(libs.ktorSerializationKotlinxJson)
}
