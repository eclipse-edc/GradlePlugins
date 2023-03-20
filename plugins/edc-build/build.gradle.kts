plugins {
    `java-gradle-plugin`
}

val jupiterVersion: String by project
val assertj: String by project
val groupId: String by project

repositories {
    mavenCentral()
    gradlePluginPortal() // needed because some plugins are only published to the Plugin Portal
}

dependencies {
    implementation(project(":plugins:autodoc:autodoc-plugin"))
    implementation(project(":plugins:test-summary"))
    implementation(project(":plugins:module-names"))
    implementation(project(":plugins:openapi-merger"))

    implementation("com.autonomousapps:dependency-analysis-gradle-plugin:1.19.0")
    implementation("io.github.gradle-nexus:publish-plugin:1.3.0")
    implementation("gradle.plugin.org.gradle.crypto:checksum:1.4.0")
    implementation("gradle.plugin.org.hidetake:gradle-swagger-generator-plugin:2.19.2")
    implementation("io.swagger.core.v3:swagger-gradle-plugin:2.2.7")
    implementation("com.rameshkp:openapi-merger-gradle-plugin:1.0.5")
}

gradlePlugin {
    website.set("https://projects.eclipse.org/proposals/eclipse-dataspace-connector")
    vcsUrl.set("https://github.com/eclipse-dataspaceconnector/GradlePlugins.git")
    // Define the plugins
    plugins {
        create("edc-build-base") {
            displayName = "edc-build-base"
            description =
                "Meta-plugin that provides the capabilities of the EDC build"
            id = "${groupId}.edc-build-base"
            implementationClass = "org.eclipse.edc.plugins.edcbuild.EdcBuildBasePlugin"
            tags.set(listOf("build", "verification", "test"))
        }
        create("edc-build") {
            displayName = "edc-build"
            description =
                "Plugin that applies the base capabilities and provides default configuration for the EDC build"
            id = "${groupId}.edc-build"
            implementationClass = "org.eclipse.edc.plugins.edcbuild.EdcBuildPlugin"
            tags.set(listOf("build", "verification", "test"))
        }
    }
}
