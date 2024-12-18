plugins {
    `java-gradle-plugin`
}

val group: String by project

dependencies {
    implementation(libs.plugin.openapi.merger.app)
    implementation(libs.plugin.openapi.merger) {
        constraints {
            implementation(libs.swagger.parser) {
                because("OpenAPI merger plugin uses an old version that caused this issue: https://github.com/eclipse-edc/GradlePlugins/issues/183")
            }
        }
    }
}

gradlePlugin {
    website.set("https://projects.eclipse.org/projects/technology.edc")
    vcsUrl.set("https://github.com/eclipse-edc/GradlePlugins.git")
    // Define the plugin
    plugins {
        create("openapi-merger") {
            displayName = "openapi-merger"
            description =
                "Plugin to several OpenAPI spec files into one"
            id = "${group}.openapi-merger"
            implementationClass = "org.eclipse.edc.plugins.openapimerger.OpenApiMergerPlugin"
            tags.set(listOf("build", "openapi", "merge", "documentation"))
        }
    }
}
