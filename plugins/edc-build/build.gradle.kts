plugins {
    `java-gradle-plugin`
    id("org.gradle.crypto.checksum") version "1.4.0"
}

val jupiterVersion: String by project
val assertj: String by project
val groupId: String by project

dependencies {
    implementation(project(":plugins:autodoc:autodoc-plugin"))
    implementation(project(":plugins:test-summary"))
    implementation(project(":plugins:module-names"))
}

gradlePlugin {
    // Define the plugin
    plugins {
        create("edc-build") {
            displayName = "edc-build"
            description =
                "Meta-plugin that configures the EDC build"
            id = "${groupId}.edc-build"
            implementationClass = "org.eclipse.dataspaceconnector.plugins.edcbuild.EdcBuildPlugin"
        }
    }
}

pluginBundle {
    website = "https://projects.eclipse.org/proposals/eclipse-dataspace-connector"
    vcsUrl = "https://github.com/eclipse-dataspaceconnector/GradlePlugins.git"
    version = version
    tags = listOf("build", "verification", "test")
}