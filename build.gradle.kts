import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

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

    testImplementation(gradleTestKit())
    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)
    testImplementation(libs.ktorClientCio)
    testImplementation(libs.ktorClientCore)
    testImplementation(libs.ktorClientJson)
    testImplementation(libs.ktorClientSerialization)
    testImplementation(libs.ktorClientContentNegotiation)
    testImplementation(libs.ktorSerializationKotlinxJson)
}

gradlePlugin {
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

dockerCompose {
    // Docker Compose v1 is needed for this Plugin to work
    useComposeFiles.set(listOf("docker-compose.yml"))
    waitForTcpPorts.set(true)
    captureContainersOutput.set(true)
    stopContainers.set(true)
    removeContainers.set(true)
    buildBeforeUp.set(true)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    systemProperty("org.gradle.testkit.dir", gradle.gradleUserHomeDir)
}

sourceSets {
    create("integrationTest") {
        withConvention(KotlinSourceSet::class) {
            kotlin.srcDir("src/integrationTest/kotlin")
            resources.srcDir("src/integrationTest/resources")
            compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
            runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
        }
    }
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs the integration tests"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    mustRunAfter(tasks["test"])
    useJUnitPlatform()
}
dockerCompose.isRequiredBy(integrationTest)

tasks.check {
    dependsOn("integrationTest")
}
