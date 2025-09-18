plugins {
    alias(libs.plugins.publish)
}

val group: String by project

repositories {
    mavenCentral()
    gradlePluginPortal() // needed because some plugins are only published to the Plugin Portal
}

dependencies {
    implementation(libs.plugin.checksum)
    implementation(libs.plugin.publish)
    implementation(libs.plugin.swagger)
    implementation(libs.openapi.generator)
    implementation(libs.plugin.openapi.generator)
    implementation(libs.swagger.parser)
    // openapi-merger deprecated, will be removed
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

    plugins {
        create("edc-build") {
            id = "${group}.edc-build"
            displayName = "edc-build"
            description =
                "Plugin that applies the base capabilities and provides default configuration for the EDC build"
            implementationClass = "org.eclipse.edc.plugins.edcbuild.EdcBuildPlugin"
            tags = listOf("build", "verification", "test")
        }
    }
}

sourceSets {
    main {
        java {
            val rootProjectDir = projectDir.resolve("..").resolve("..")
            srcDir(rootProjectDir.resolve("buildSrc").resolve("build").resolve("generated").resolve("sources"))
        }
    }
}

edcBuild {
    publish.set(false)
}
