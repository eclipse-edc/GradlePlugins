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

publishing {
    afterEvaluate {
        // values needed for publishing
        val pluginsWebsiteUrl: String by project
        val pluginsDeveloperId: String by project
        val pluginsDeveloperName: String by project
        val pluginsDeveloperEmail: String by project
        val pluginsScmConnection: String by project
        val pluginsScmUrl: String by project
        publishing {
            publications.forEach { i ->
                val mp = (i as MavenPublication)
                mp.pom {
                    name.set(project.name)
                    description.set("edc :: ${project.name}")
                    url.set(pluginsWebsiteUrl)

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                        developers {
                            developer {
                                id.set(pluginsDeveloperId)
                                name.set(pluginsDeveloperName)
                                email.set(pluginsDeveloperEmail)
                            }
                        }
                        scm {
                            connection.set(pluginsScmConnection)
                            url.set(pluginsScmUrl)
                        }
                    }
                }
//                println("\nset POM for: ${mp.groupId}:${mp.artifactId}:${mp.version}")
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
            credentials {
                username = System.getenv("OSSRH_USER") ?: return@credentials
                password = System.getenv("OSSRH_PASSWORD") ?: return@credentials
            }
        }
    }
}