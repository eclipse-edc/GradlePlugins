plugins {
    `java-gradle-plugin`
}

val jupiterVersion: String by project
val assertj: String by project
val groupId: String by project

dependencies {
    // contains the actual merger task
    implementation("com.rameshkp:openapi-merger-gradle-plugin:1.0.5")
    // needed for the OpenApiDataInvalidException:
    implementation("com.rameshkp:openapi-merger-app:1.0.5")
}

gradlePlugin {
    website.set("https://projects.eclipse.org/proposals/eclipse-dataspace-connector")
    vcsUrl.set("https://github.com/eclipse-dataspaceconnector/GradlePlugins.git")
    // Define the plugin
    plugins {
        create("openapi-merger") {
            displayName = "openapi-merger"
            description =
                "Plugin to several OpenAPI spec files into one"
            id = "${groupId}.openapi-merger"
            implementationClass = "org.eclipse.edc.plugins.openapimerger.OpenApiMergerPlugin"
            tags.set(listOf("build", "openapi", "merge", "documentation"))
        }
    }
}
