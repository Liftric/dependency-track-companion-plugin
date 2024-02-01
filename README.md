# Gradle Dependency Track Companion w Plugin

This Gradle plugin is designed to ease the process of working with [Dependency Track](https://dependencytrack.org/), a Continuous SBOM Analysis Platform. With this plugin, you can automate the upload process of SBOM files, generate Vex files for component or vulnerability suppression, and more.
This plugin internally applies the [CycloneDX Gradle plugin](https://github.com/CycloneDX/cyclonedx-gradle-plugin), so you don't need to manually include it in your project.

## Features

The plugin offers several tasks:

- `createProject`: Creates a Project
- `generateSbom`: Generates the SBOM (Runs "cyclonedxBom" from [cyclonedx-gradle-plugin](https://github.com/CycloneDX/cyclonedx-gradle-plugin) under the hood)
- `uploadSbom`: Uploads SBOM file.
- `generateVex`: Generates VEX file.
- `uploadVex`: Uploads VEX file.
- `analyzeProject`: Triggers Vulnerability Analysis on a specific project
- `riskScore`: Gets risk score. If the risk score is higher than the specified value, the task will fail.
- `getOutdatedDependencies`: Gets outdated dependencies.
- `getSuppressedVuln`: Gets suppressed vulnerabilities.
- `runDepTrackWorkflow`: Runs `generateSbom`, `uploadSbom`, `generateVex` and `uploadVex` tasks for CI/CD.

### Task Configuration

Each task requires certain inputs which are to be specified in your `build.gradle.kts`. The configuration for each task is as follows:

#### createProject

- `url`: Dependency Track API URL
- `apiKey`: Dependency Track API KEY
- `projectName`: The Name of the Project you want to create
- `projectVersion`: *Optional* - The Version of the Project you want to create
- `projectActive`: *Optional* - default is true, set to false to create an inactive Project
- `projectTags`: *Optional* - add Tags to your Project
- `parentUUID`: *Optional* - Used for creating in a parent project
- `ignoreProjectAlreadyExists`: *Optional* - default is false, set to true to ignore "Project already exist" error

#### uploadSbom

- `url`: Dependency Track API URL
- `apiKey`: Dependency Track API KEY
- `inputFile`: *Optional* - Default: build/reports/bom.json
- `autoCreate`: *Optional* - Default: false
- `projectUUID`: *Optional* - You need to set UUID or projectName and projectVersion
- `projectName`: *Optional* - You need to set UUID or projectName and projectVersion
- `projectVersion`: *Optional* - You need to set UUID or projectName and projectVersion
- `parentUUID`: *Optional* - Used for creating in a parent project 
- `parentName`: *Optional* - Used for creating in a parent project 
- `parentVersion`: *Optional* - Used for creating in a parent project

#### generateVex

- `vexComponent`: *Optional* - For suppressing vulnerabilities in one component
- `vexVulnerability`: *Optional* - For suppressing vulnerabilities in all components
- `inputFile`: *Optional* - Default: build/reports/bom.json
- `outputFile`: *Optional* - Default: build/reports/vex.json

#### uploadVex

- `url`: Dependency Track API URL
- `apiKey`: Dependency Track API KEY
- `outputFile`: *Optional* (Default "build/reports/vex.json")
- `projectUUID`: *Optional* - You need to set UUID or projectName and projectVersion
- `projectName`: *Optional* - You need to set UUID or projectName and projectVersion
- `projectVersion`: *Optional* - You need to set UUID or projectName and projectVersion

#### riskScore

- `url`: Dependency Track API URL
- `apiKey`: Dependency Track API KEY
- `projectUUID`: *Optional* - You need to set UUID or projectName and projectVersion
- `projectName`: *Optional* - You need to set UUID or projectName and projectVersion
- `projectVersion`: *Optional* - You need to set UUID or projectName and projectVersion
- `riskScore`: *Optional* - Used for failing the task if the risk score is higher than the specified value.
   - `timeout`: *Optional* - If specified, the task will wait for the risk score to be calculated. Default: 0 seconds
   - `maxRiskScore`: *Optional* - If specified, the task will fail if the risk score is higher than the specified value.

#### analyzeProject

- `url`: Dependency Track API URL
- `apiKey`: Dependency Track API KEY
- `projectUUID`: *Optional* - You need to set UUID or projectName and projectVersion
- `projectName`: *Optional* - You need to set UUID or projectName and projectVersion
- `projectVersion`: *Optional* - You need to set UUID or projectName and projectVersion

#### getOutdatedDependencies

- `url`: Dependency Track API URL
- `apiKey`: Dependency Track API KEY
- `projectUUID`: *Optional* - You need to set UUID or projectName and projectVersion
- `projectName`: *Optional* - You need to set UUID or projectName and projectVersion
- `projectVersion`: *Optional* - You need to set UUID or projectName and projectVersion

#### getSuppressedVuln

- `url`: Dependency Track API URL
- `apiKey`: Dependency Track API KEY
- `projectUUID`: *Optional* - You need to set UUID or projectName and projectVersion
- `projectName`: *Optional* - You need to set UUID or projectName and projectVersion
- `projectVersion`: *Optional* - You need to set UUID or projectName and projectVersion

## Example Configuration

Here's how you can configure all tasks:

```kotlin
import com.liftric.dtcp.extensions.*
import org.cyclonedx.model.vulnerability.Vulnerability

val version: String by project
val name: String by project
dependencyTrackCompanion {
    url.set("https://api.dtrack.example.com")
    apiKey.set(System.getenv("DT_API_KEY"))
    autoCreate.set(true)
    projectName.set(name)
    projectVersion.set(version)
    parentName.set(name)
    riskScore{
        timeout.set(20.seconds)
        maxRiskScore.set(7.0)
    }
    vexComponent {
        purl.set("pkg:maven/org.eclipse.jetty/jetty-http@9.4.49.v20220914?type=jar")
        vulnerability {
            id.set("CVE-2023-26048")
            source.set("NVD")
            analysis.set(Vulnerability.Analysis.State.FALSE_POSITIVE)
        }
    }
    vexVulnerability {
        id.set("CVE-2020-8908")
        source.set("NVD")
        analysis.set(Vulnerability.Analysis.State.RESOLVED)
        detail.set("This is resolved")
    }
}
```

## License

This Gradle Dependency Track Plugin is released under MIT License.

This project is not a derivative of [Dependency Track](https://dependencytrack.org/), but a tool that interacts with it. Please note that Dependency Track is released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0). Refer to their respective licenses for more information.
