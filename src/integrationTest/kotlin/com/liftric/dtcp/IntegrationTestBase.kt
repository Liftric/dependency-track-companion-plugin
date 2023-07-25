package com.liftric.dtcp

import com.liftric.dtcp.model.VexComponent
import com.liftric.dtcp.model.VexVulnerability

/**
 * This base class provides common resources for integration tests.
 * By using this base class, resources don't need to be recreated for each individual test,
 * thus preventing duplication of code and unnecessary instantiation of resources in each test.
 */

abstract class IntegrationTestBase {
    val dependencyTrackApiEndpoint = "http://localhost:8081"

    val vexComponent = VexComponent(
        purl = "pkg:maven/org.jetbrains.kotlin/kotlin-stdlib-jdk8@1.8.21?type=jar",
        vulnerability = VexVulnerability(
            id = "CVE-2023-26048",
            source = "NVD",
            analysis = "Vulnerability.Analysis.State.FALSE_POSITIVE",
            analysisValue = "FALSE_POSITIVE",
            detail = null,
        )
    )

    val vexVulnerability = VexVulnerability(
        id = "CVE-2020-8908",
        source = "NVD",
        analysis = "Vulnerability.Analysis.State.RESOLVED",
        analysisValue = "RESOLVED",
        detail = "This is resolved",
    )
}
