rootProject.name = "dependency-track-companion-plugin"

pluginManagement {
    dependencyResolutionManagement {
        versionCatalogs {
            create("libs") {
                version("kotlin", "1.8.21")
                version("ktor", "2.3.0")
                version("cyclonedx-core-java", "7.3.2")
                version("junit-bom", "5.9.3")

                plugin("dockerCompose", "com.avast.gradle.docker-compose").version("0.16.12")
                plugin("kotlinJvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
                plugin("kotlinSerialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
                plugin("gradlePluginPublish", "com.gradle.plugin-publish").version("1.2.0")

                library("kotlinStdlibJdk8", "org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")
                library("cyclonedxCoreJava", "org.cyclonedx", "cyclonedx-core-java").versionRef("cyclonedx-core-java")
                library("kotlinBom", "org.jetbrains.kotlin", "kotlin-bom").versionRef("kotlin")
                library("ktorClientCio", "io.ktor", "ktor-client-cio").versionRef("ktor")
                library("ktorClientCore", "io.ktor", "ktor-client-core").versionRef("ktor")
                library("ktorClientJson", "io.ktor", "ktor-client-json").versionRef("ktor")
                library("ktorClientSerialization", "io.ktor", "ktor-client-serialization").versionRef("ktor")
                library(
                    "ktorClientContentNegotiation",
                    "io.ktor",
                    "ktor-client-content-negotiation"
                ).versionRef("ktor")
                library(
                    "ktorSerializationKotlinxJson",
                    "io.ktor",
                    "ktor-serialization-kotlinx-json"
                ).versionRef("ktor")
                library("kotlinReflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
                library("junitBom", "org.junit", "junit-bom").versionRef("junit-bom")
                library("junitJupiter", "org.junit.jupiter", "junit-jupiter").versionRef("junit-bom")
            }
        }
    }
}
