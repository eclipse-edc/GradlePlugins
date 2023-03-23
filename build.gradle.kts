plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    checkstyle
    `maven-publish`
    signing
    `java-library`
    `version-catalog`
    alias(libs.plugins.publish) apply false
}

val groupId: String by project
val annotationProcessorVersion: String by project

allprojects {
    apply(plugin = "org.eclipse.edc.edc-build")
    group = groupId

    configure<org.eclipse.edc.plugins.autodoc.AutodocExtension> {
        processorVersion.set(annotationProcessorVersion)
        outputDirectory.set(project.buildDir)
    }

    // for all gradle plugins:
    pluginManager.withPlugin("java-gradle-plugin") {
        apply(plugin = "com.gradle.plugin-publish")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }

    // configure checkstyle version
    checkstyle {
        toolVersion = "10.0"
        maxErrors = 0 // does not tolerate errors
    }

    repositories {
        mavenCentral()
    }

    // let's not generate any reports because that is done from within the Github Actions workflow
    tasks.withType<Checkstyle> {
        reports {
            html.required.set(false)
            xml.required.set(true)
        }
    }

    tasks.withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        metaInf {
            from("${rootProject.projectDir.path}/NOTICE.md")
            from("${rootProject.projectDir.path}/LICENSE")
        }
    }
}
