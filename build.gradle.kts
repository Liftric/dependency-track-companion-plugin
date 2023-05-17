@Suppress("DSL_SCOPE_VIOLATION") // IntelliJ incorrectly marks libs as not callable
plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.dockerCompose)
}

val dtApiKey: String by project

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
    check {
        dependsOn(integrationTestTask)
    }
}

gradlePlugin {
    testSourceSets(integrationTest)
    plugins {
        create("dependency-track-companion-plugin") {
            id = "${project.property("pluginGroup")}.${project.property("pluginName")}"
            implementationClass = "${project.property("pluginGroup")}.DepTrackHelperPlugin"
            displayName = project.property("pluginName").toString()
            version = project.property("pluginVersion").toString()
            group = project.property("pluginGroup").toString()
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

    testImplementation(libs.junitJupiter)

    "integrationTestImplementation"(gradleTestKit())
    "integrationTestImplementation"(libs.cyclonedxCoreJava)
    "integrationTestImplementation"(libs.junitJupiter)
    "integrationTestImplementation"(libs.ktorClientCio)
    "integrationTestImplementation"(libs.ktorClientContentNegotiation)
    "integrationTestImplementation"(libs.ktorSerializationKotlinxJson)
}

