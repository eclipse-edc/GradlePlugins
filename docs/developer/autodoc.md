# The `autodoc` Gradle Plugin

## Introduction

The `autodoc` plugin hooks into the Java compiler task (`compileJava`) and generates a module
manifest file that contains meta information about each module.
For example, it exposes all required and provided dependencies of an EDC `ServiceExtension`.

## Module structure

The `autodoc` plugin is located at `plugins/autodoc` and consists of four separate modules:

- `autodoc-plugin`: contains the actual Gradle `Plugin` and an `Extension` to configure the plugin. This module is
  published to the [Gradle Portal](https://plugins.gradle.org).
- `autodoc-processor`: contains an `AnnotationProcessor` that hooks into the compilation process and builds the manifest
  file. Published to MavenCentral.
- `autodoc-extension-test`: test code for the annotation processor. Not published.
- `autodoc-spi-test`: test code for the annotation processor. Not published.

## Usage

In order to use the `autodoc` plugin we must follow a few simple steps. All examples use the Kotlin DSL.

### Add the plugin to the `buildscript` block of your `build.gradle.kts`:

   ```kotlin
   buildscript {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
    dependencies {
        classpath("org.eclipse.dataspaceconnector.autodoc:org.eclipse.dataspaceconnector.autodoc.gradle.plugin:<VERSION>>")
    }
}
   ```

Please note that the `repositories` configuration can be omitted, if the release version of the plugin is used.

### Apply the plugin to the project:

There are two options to apply a plugin. For multi-module builds this should be done at the root level.

1. via `plugin` block:
   ```kotlin
   plugins {
       id("org.eclipse.dataspaceconnector.autodoc")
   }
   ```
2. using the iterative approach, useful when applying to `allprojects` or `subprojects`:
   ```kotlin
   subprojects{
      apply(plugin = "org.eclipse.dataspaceconnector.autodoc")
   }
   ```

### Configure the plugin [optional]

The `autodoc` plugin exposes the following configuration values:

1. the `processorVersion`: tells the plugin, which version of the annotation processor module to use. If this is
   omitted, the plugin will use its own version. Please enter just the SemVer-compliant version information,
   no `groupId` or `artifactName` are needed.
   ```kotlin
   configure<org.eclipse.dataspaceconnector.plugins.autodoc.AutodocExtension> {
       processorVersion.set("<VERSION>")
   }
   ```

_The plugin will then generate an `edc.json` file for every module/gradle project._