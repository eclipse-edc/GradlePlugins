plugins {
    `java-gradle-plugin`
}

val groupId = "org.eclipse.edc"

group = groupId

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.autonomousapps:dependency-analysis-gradle-plugin:1.19.0")
    implementation("io.github.gradle-nexus:publish-plugin:1.3.0")
    implementation("gradle.plugin.org.gradle.crypto:checksum:1.4.0")
    implementation("gradle.plugin.org.hidetake:gradle-swagger-generator-plugin:2.19.2")
    implementation("io.swagger.core.v3:swagger-gradle-plugin:2.2.7")
    implementation("com.rameshkp:openapi-merger-gradle-plugin:1.0.5")
    implementation("com.rameshkp:openapi-merger-app:1.0.5")
    implementation("org.eclipse.edc:runtime-metamodel:0.0.1-milestone-8")

    implementation(libs.jetbrains.annotations)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatypeJsr310)
}

gradlePlugin {
    plugins {

        create("bbbuild") {
            id = "org.eclipse.edc.edc-build"
            implementationClass = "org.eclipse.edc.plugins.edcbuild.EdcBuildPlugin"
        }
    }
}



sourceSets {
    main {
        java {
            srcDirs(
                "../plugins/autodoc/autodoc-plugin/src/main",
                "../plugins/autodoc/autodoc-processor/src/main",
                "../plugins/edc-build/src/main",
                "../plugins/module-names/src/main",
                "../plugins/openapi-merger/src/main",
                "../plugins/test-summary/src/main"
            )
        }
    }
}


