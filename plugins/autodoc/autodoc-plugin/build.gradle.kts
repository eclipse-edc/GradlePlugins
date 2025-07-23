plugins {
    `java-gradle-plugin`
}

dependencies {
    implementation(libs.jetbrains.annotations)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(project(":plugins:autodoc:autodoc-converters"))
}

val group: String by project

gradlePlugin {
    website.set("https://projects.eclipse.org/projects/technology.edc")
    vcsUrl.set("https://github.com/eclipse-edc/GradlePlugins.git")

    plugins {
        create("autodoc") {
            displayName = "autodoc"
            description =
                "Plugin to generate a documentation manifest for the EDC Metamodel, i.e. extensions, SPIs, etc."
            id = "${group}.autodoc"
            implementationClass = "org.eclipse.edc.plugins.autodoc.AutodocPlugin"
            tags.set(listOf("build", "documentation", "generated", "autodoc"))
        }
    }
}
