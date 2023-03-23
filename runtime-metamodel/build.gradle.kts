plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(libs.jetbrains.annotations)
    api(libs.jackson.core)
    api(libs.jackson.annotations)
    api(libs.jackson.databind)
    api(libs.jackson.datatypeJsr310)
}

configure<org.eclipse.edc.plugins.autodoc.AutodocExtension> {
    excluded.set(true)
}
