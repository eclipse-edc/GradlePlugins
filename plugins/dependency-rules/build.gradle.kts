plugins {
    `java-gradle-plugin`
    id("org.gradle.crypto.checksum") version "1.4.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal() // needed because some plugins are only published to the Plugin Portal
}

val groupId: String by project

gradlePlugin {
    // Define the plugins
    plugins {
        create("DependencyRulesPlugin") {
            displayName = "dependency-rules"
            description =
                "Plugin to verify that there aren't circular dependencies"
            id = "${groupId}.dependency-rules"
            implementationClass = "org.eclipse.edc.plugins.dependencyrules.DependencyRulesPlugin"
        }
    }
}

pluginBundle {
    website = "https://projects.eclipse.org/proposals/eclipse-dataspace-connector"
    vcsUrl = "https://github.com/eclipse-dataspaceconnector/GradlePlugins.git"
    version = version
    tags = listOf("build", "verification", "test")
}
