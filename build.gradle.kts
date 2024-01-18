import java.nio.file.Paths

plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    checkstyle
    `maven-publish`
    signing
    `java-library`
    `version-catalog`
    alias(libs.plugins.publish) apply false
}

val group: String by project
val annotationProcessorVersion: String by project
val edcScmUrl: String by project
val edcScmConnection: String by project

allprojects {
    apply(plugin = "${group}.edc-build")

    configure<org.eclipse.edc.plugins.autodoc.AutodocExtension> {
        processorVersion.set(annotationProcessorVersion)
        outputDirectory.set(project.buildDir)
    }

    // for all gradle plugins:
    pluginManager.withPlugin("java-gradle-plugin") {
        apply(plugin = libs.plugins.publish.get().pluginId)
    }

    configure<org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension> {
        pom {
            scmUrl.set(edcScmUrl)
            scmConnection.set(edcScmConnection)
        }
    }

    // configure checkstyle version
    checkstyle {
        toolVersion = "10.0"
        maxErrors = 0 // does not tolerate errors
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

    // FIXME - workaround for https://github.com/gradle/gradle/issues/26091
    val signingTasks = tasks.withType<Sign>()
    tasks.withType<AbstractPublishToMaven>().configureEach {
        mustRunAfter(signingTasks)
    }
}
