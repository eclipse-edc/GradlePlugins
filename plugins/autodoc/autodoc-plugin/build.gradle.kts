plugins {
    `java-gradle-plugin`
}

val jupiterVersion: String by project
val assertj: String by project
val groupId: String by project

gradlePlugin {
    // Define the plugin
    plugins {
        create("autodoc") {
            displayName = "autodoc"
            description =
                "Plugin to generate a documentation manifest for the EDC Metamodel, i.e. extensions, SPIs, etc."
            id = "autodoc"
            group = groupId
            implementationClass = "org.eclipse.dataspaceconnector.plugins.autodoc.AutodocPlugin"
        }
    }
}



pluginBundle {
    website = "https://projects.eclipse.org/proposals/eclipse-dataspace-connector"
    vcsUrl = "http://github.com/eclipse-dataspaceconnector/GradlePlugins"
    group = groupId
    version = version
    tags = listOf("build", "documentation", "generated", "autodoc")
}