plugins {
    `java-gradle-plugin`
}

dependencies {
    implementation(libs.jetbrains.annotations)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatypeJsr310)
}

val jupiterVersion: String by project
val assertj: String by project
val groupId: String by project

gradlePlugin {
    website.set("https://projects.eclipse.org/proposals/eclipse-dataspace-connector")
    vcsUrl.set("https://github.com/eclipse-dataspaceconnector/GradlePlugins.git")

    // Define the plugin
    plugins {
        create("autodoc") {
            displayName = "autodoc"
            description =
                "Plugin to generate a documentation manifest for the EDC Metamodel, i.e. extensions, SPIs, etc."
            id = "${groupId}.autodoc"
            implementationClass = "org.eclipse.edc.plugins.autodoc.AutodocPlugin"
            tags.set(listOf("build", "documentation", "generated", "autodoc"))
        }
    }
}
