plugins {
    `java-gradle-plugin`
    id("org.gradle.crypto.checksum") version "1.4.0"
}

val jupiterVersion: String by project
val assertj: String by project
val groupId: String by project

gradlePlugin {
    website.set("https://projects.eclipse.org/proposals/eclipse-dataspace-connector")
    vcsUrl.set("https://github.com/eclipse-dataspaceconnector/GradlePlugins.git")
    // Define the plugin
    plugins {
        create("module-names") {
            displayName = "module-names"
            description =
                "Plugin to verify that a project has no duplicate submodules (by name)"
            id = "${groupId}.module-names"
            implementationClass = "org.eclipse.edc.plugins.modulenames.ModuleNamesPlugin"
            tags.set(listOf("build", "verification"))
            version = version
        }
    }
}