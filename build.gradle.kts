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
}

gradlePlugin {
    plugins {
        create("dependency-track-companion-plugin") {
            id = "${project.property("pluginGroup")}.${project.property("pluginName")}"
            implementationClass = "${project.property("pluginGroup")}.DepTrackHelperPlugin"
            version = project.property("pluginVersion").toString()
            group = project.property("pluginGroup").toString()
        }
    }
}

dockerCompose {
    useComposeFiles.set(listOf("docker-compose.yml"))
    waitForTcpPorts.set(true)
    captureContainersOutput.set(true)
    stopContainers.set(true)
    removeContainers.set(true)
    buildBeforeUp.set(true)
}

//DepTrackHelperPlugin {
//}

//tasks.test {
//    useJUnitPlatform()
//    testLogging {
//        events("passed", "skipped", "failed")
//    }
//}
//
//tasks.withType<JavaCompile>().configureEach {
//    options.release.set(8)
//}
//
//// config JVM target to 1.8 for kotlin compilation tasks
//tasks.withType<KotlinCompile>().configureEach {
//    kotlinOptions.jvmTarget = "1.8"
//}