plugins {
    `java-gradle-plugin`
}

val jupiterVersion: String by project
val assertj: String by project

dependencies {
    implementation(project(":runtime-metamodel"))
}

gradlePlugin {
    // Define the plugin
    plugins {
        create("autodoc") {
            id = "autodoc"
            implementationClass = "org.eclipse.dataspaceconnector.plugins.autodoc.AutodocPlugin"
        }
    }
}

val groupId: String by project

// Running the functionalTest with Junit 5 seems not to work as of now. Once it does, the following lines can be uncommented

// Add a source set and a task for a functional test suite
//val functionalTest: SourceSet by sourceSets.creating
//gradlePlugin.testSourceSets(functionalTest)
//
//configurations[functionalTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
//
//val functionalTestTask = tasks.register<Test>("functionalTest") {
//    testClassesDirs = functionalTest.output.classesDirs
//    classpath = configurations[functionalTest.runtimeClasspathConfigurationName] + functionalTest.output
//}

//tasks.check {
// Run the functional tests as part of `check`
//    dependsOn(functionalTestTask)
//}

pluginBundle {
    website = "https://projects.eclipse.org/proposals/eclipse-dataspace-connector"
    vcsUrl = "http://github.com/eclipse-dataspaceconnector/"
    group = groupId
    version = version
    tags = listOf("build", "documentation", "generated", "autodoc")
}
