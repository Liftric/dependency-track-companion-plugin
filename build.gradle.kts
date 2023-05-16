import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    kotlin("plugin.serialization").version("1.8.20")
    id("com.avast.gradle.docker-compose") version "0.16.12"
}

val ktorVersion: String by project
val kotlinVersion: String by project
val dtApiKey: String by project
version = project.property("pluginVersion").toString()
group = project.property("pluginGroup").toString()

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.cyclonedx:cyclonedx-core-java:7.3.2")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    testImplementation(platform("org.junit:junit-bom:5.9.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(gradleTestKit())
    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")
    testImplementation("io.ktor:ktor-client-core:$ktorVersion")
    testImplementation("io.ktor:ktor-client-json:$ktorVersion")
    testImplementation("io.ktor:ktor-client-serialization:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
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
