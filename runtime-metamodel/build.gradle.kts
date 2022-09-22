plugins {
    `java-library`
    `maven-publish`
}

val jupiterVersion: String by project
val assertj: String by project
val jetBrainsAnnotationsVersion: String by project
val jacksonVersion: String by project

dependencies {

    api("org.jetbrains:annotations:${jetBrainsAnnotationsVersion}")
    api("com.fasterxml.jackson.core:jackson-core:${jacksonVersion}")
    api("com.fasterxml.jackson.core:jackson-annotations:${jacksonVersion}")
    api("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")

    // Use JUnit test framework for unit tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testImplementation("org.assertj:assertj-core:${assertj}")
    implementation("org.assertj:assertj-core:${assertj}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}

publishing {
    publications {
        create<MavenPublication>("runtime-metamodel") {
            artifactId = "runtime-metamodel"
            from(components["java"])
        }
    }
}