plugins {
    `java-gradle-plugin`
}

val jupiterVersion: String by project
val assertj: String by project

dependencies {
    implementation(project(":runtime-metamodel"))
    // Use JUnit test framework for unit tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testImplementation("org.assertj:assertj-core:${assertj}")
    implementation("org.assertj:assertj-core:${assertj}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
}

gradlePlugin {
    // Define the plugin
    plugins {
        create("autodoc") {
            id = "autodoc"
            implementationClass = "org.eclipse.dataspaceconnector.plugins.AutodocPlugin"
        }
    }
}

val groupId: String by project
// Add a source set and a task for a functional test suite
val functionalTest: SourceSet by sourceSets.creating
gradlePlugin.testSourceSets(functionalTest)

configurations[functionalTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())

val functionalTestTask = tasks.register<Test>("functionalTest") {
    testClassesDirs = functionalTest.output.classesDirs
    classpath = configurations[functionalTest.runtimeClasspathConfigurationName] + functionalTest.output
}

tasks.check {
    // Run the functional tests as part of `check`
    dependsOn(functionalTestTask)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}

pluginBundle {
    website = "https://projects.eclipse.org/proposals/eclipse-dataspace-connector"
    vcsUrl = "http://github.com/eclipse-dataspaceconnector/"
    group = groupId
    version = version
    tags = listOf("build", "documentation", "generated", "autodoc")
}
