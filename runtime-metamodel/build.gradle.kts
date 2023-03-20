plugins {
    `java-library`
    `maven-publish`
}

dependencies {

    api(libs.jetbrains.annotations)
    api(libs.jackson.core)
    api(libs.jackson.annotations)
    api(libs.jackson.databind)
    api(libs.jackson.datatypeJsr310)

}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishing {
    publications {
        create<MavenPublication>("runtime-metamodel") {
            artifactId = "runtime-metamodel"
            from(components["java"])
        }
    }
}
