# Gradle Dependency Track Companion w Plugin

This Gradle plugin is designed to ease the process of working with [Dependency Track](https://dependencytrack.org/), a Continuous SBOM Analysis Platform. With this plugin, you can automate the upload process of SBOM files, generate Vex files for component or vulnerability suppression, and more.

## Features

The plugin offers several tasks:

- `generateVex`: Generates VEX file.
- `getOutdatedDependencies`: Gets outdated dependencies.
- `getSuppressedVuln`: Gets suppressed vulnerabilities.
- `runDepTrackWorkflow`: Runs `uploadSbom`, `generateVex` and `uploadVex` tasks for CI/CD.
- `uploadSbom`: Uploads SBOM file.
- `uploadVex`: Uploads VEX file.

### Task Configuration

Each task requires certain inputs which are to be specified in your `build.gradle.kts`. The configuration for each task is as follows:

#### uploadSbom

- `url`: Dependency Track API URL
- `apiKey`: Dependency Track API KEY
- `inputFile`: *Optional* - Default: build/reports/bom.json
- `uploadSbom`: [Dependency Track BOM Upload Api Reference](https://yoursky.blue/documentation-api/dependencytrack.html#tag/bom/operation/UploadBom)

#### generateVex

- `vexComponent`: *Optional* - For suppressing vulnerabilities in one component
- `vexVulnerability`: *Optional* - For suppressing vulnerabilities in all components
- `inputFile`: *Optional* - Default: build/reports/bom.json
- `outputFile`: *Optional* - Default: build/reports/vex.json

#### uploadVex

- `url`: Dependency Track API URL
- `apiKey`: Dependency Track API KEY
- `outputFile`: *Optional* (Default "build/reports/vex.json")
- `uploadVex`: [Dependency Track VEX Upload API Reference](https://yoursky.blue/documentation-api/dependencytrack.html#tag/vex/operation/uploadVex)

#### riskScore

- `url`: Dependency Track API URL
- `apiKey`: Dependency Track API KEY
- `riskScore`: *Optional* - [Dependency Track Project Lookup API Reference](https://yoursky.blue/documentation-api/dependencytrack.html#tag/project/operation/getProjectByNameAndVersion)
   - `timeout`: *Optional* - If specified, the task will wait for the risk score to be calculated. Default: 0 seconds
   - `maxRiskScore`: *Optional* - If specified, the task will fail if the risk score is higher than the specified value.

#### runDepTrackWorkflow

- This task requires configuration for `uploadSbom`, `generateVex`, and `uploadVex`.
- Runs `uploadSbom`, `generateVex`, `uploadVex` and `riskScore` tasks for CI/CD.

#### getOutdatedDependencies

- `url`: Dependency Track API URL
- `apiKey`: Dependency Track API KEY
- `getOutdatedDependencies`: [Dependency Track Project Lookup API Reference](https://yoursky.blue/documentation-api/dependencytrack.html#tag/project/operation/getProjectByNameAndVersion)

#### getSuppressedVuln

- `url`: Dependency Track API URL
- `apiKey`: Dependency Track API KEY
- `getSuppressedVuln`: [Dependency Track Project Lookup API Reference](https://yoursky.blue/documentation-api/dependencytrack.html#tag/project/operation/getProjectByNameAndVersion)

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
    uploadSBOM {
        autoCreate.set(true)
        projectName.set(name)
        projectVersion.set(version)
        parentName.set(name)
    }
    uploadVex {
        projectName.set(name)
        projectVersion.set(version)
    }
    getOutdatedDependencies {
        projectName.set(name)
        projectVersion.set(version)
    }
    getSuppressedVuln {
        projectName.set(name)
        projectVersion.set(version)
    }
    riskScore{
        projectName.set(name)
        projectVersion.set(version)
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
