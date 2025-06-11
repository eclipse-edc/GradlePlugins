# Gradle Plugins for EDC

[![documentation](https://img.shields.io/badge/documentation-8A2BE2?style=flat-square)](https://eclipse-edc.github.io)
[![discord](https://img.shields.io/badge/discord-chat-brightgreen.svg?style=flat-square&logo=discord)](https://discord.gg/n4sD9qtjMQ)
[![latest version](https://img.shields.io/maven-central/v/org.eclipse.edc/boot?logo=apache-maven&style=flat-square&label=latest%20version)](https://search.maven.org/artifact/org.eclipse.edc/boot)
[![latest version (plugin)](https://img.shields.io/gradle-plugin-portal/v/org.eclipse.edc.edc-build)](https://plugins.gradle.org/plugin/org.eclipse.edc.edc-build)
[![license](https://img.shields.io/github/license/eclipse-edc/GradlePlugins?style=flat-square&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)
<br>
[![build](https://img.shields.io/github/actions/workflow/status/eclipse-edc/GradlePlugins/verify.yaml?branch=main&logo=GitHub&style=flat-square&label=ci)](https://github.com/eclipse-edc/GradlePlugins/actions/workflows/verify.yaml?query=branch%3Amain)
[![snapshot build](https://img.shields.io/github/actions/workflow/status/eclipse-edc/GradlePlugins/trigger_snapshot.yml?branch=main&logo=GitHub&style=flat-square&label=snapshot-build)](https://github.com/eclipse-edc/GradlePlugins/actions/workflows/trigger_snapshot.yml)
[![nightly build](https://img.shields.io/github/actions/workflow/status/eclipse-edc/GradlePlugins/nightly.yml?branch=main&logo=GitHub&style=flat-square&label=nightly-build)](https://github.com/eclipse-edc/GradlePlugins/actions/workflows/nightly.yml)

---

This repository contains various plugins for the EDC Gradle plugins.

There are 2 plugins: `edc-build` and `autodoc`

### edc-build
Is a plugin that provides basic capabilities to check/build/publish edc related modules.

The plugin is published on the https://plugins.gradle.org/plugin/org.eclipse.edc.edc-build with its own version number,
detached from the EDC version.

### autodoc
Is a plugin that permits automatic documentation generation for EDC related modules.

Its version is aligned with the EDC one. 

## Documentation

Base documentation can be found on the [documentation website](https://eclipse-edc.github.io). \
Developer documentation can be found under [docs/developer](docs/developer), \ 
where the main concepts and decisions are captured as [decision records](docs/developer/decision-records).

## Contributing

See [how to contribute](https://github.com/eclipse-edc/eclipse-edc.github.io/blob/main/CONTRIBUTING.md).
