/*
 *  Copyright (c) 2026 Think-it GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Think-it GmbH - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.edcbuild.conventions;

import org.eclipse.edc.plugins.edcbuild.EdcBuildPlugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

class MavenPublicationConventionTest {

    @TempDir
    private Path tempDir;

    @Test
    void shouldCreatePublicationAfterHooksRegisteredDuringEvaluation_whenSnapshot() throws IOException {
        var project = createModule("0.1.0-SNAPSHOT");

        // mimics autodoc: adds an artifact to the publication, which locks the configurations of the published component
        var artifact = Files.writeString(tempDir.resolve("edc.json"), "{}").toFile();
        project.afterEvaluate(p -> publications(p).configureEach(pub -> pub.getArtifacts().artifact(artifact)));

        // mimics shadow: mutates an outgoing configuration in an afterEvaluate hook registered by the module build script
        project.afterEvaluate(p -> p.getConfigurations().getByName("runtimeElements")
                .attributes(a -> a.attribute(Attribute.of("test", String.class), "value")));

        assertThatNoException().isThrownBy(() -> ((ProjectInternal) project).evaluate());
        assertThat(publications(project)).isNotEmpty();
    }

    @Test
    void shouldNotPublishJavadoc_whenSnapshot() throws IOException {
        var project = createModule("0.1.0-SNAPSHOT");

        ((ProjectInternal) project).evaluate();

        assertThat(publications(project)).isNotEmpty().allSatisfy(pub -> assertThat(pub.getArtifacts())
                .noneMatch(a -> "javadoc".equals(a.getClassifier())));
    }

    @Test
    void shouldPublishJavadoc_whenRelease() throws IOException {
        var project = createModule("0.1.0");

        ((ProjectInternal) project).evaluate();

        assertThat(publications(project)).isNotEmpty().allSatisfy(pub -> assertThat(pub.getArtifacts())
                .anyMatch(a -> "javadoc".equals(a.getClassifier())));
    }

    private Project createModule(String version) throws IOException {
        var root = ProjectBuilder.builder().withProjectDir(tempDir.resolve("root").toFile()).build();
        var moduleDir = tempDir.resolve("root").resolve("app").toFile();
        assertThat(moduleDir.mkdirs()).isTrue();
        // the convention only publishes modules having a "build.gradle.kts", but the Kotlin DSL is not available to
        // ProjectBuilder, so an empty groovy script (which takes precedence) is the one that actually gets evaluated
        assertThat(new File(moduleDir, "build.gradle.kts").createNewFile()).isTrue();
        assertThat(new File(moduleDir, "build.gradle").createNewFile()).isTrue();

        var module = ProjectBuilder.builder().withParent(root).withName("app").withProjectDir(moduleDir).build();
        module.setVersion(version);
        module.setGroup("org.eclipse.edc.test");
        module.getPlugins().apply(EdcBuildPlugin.class);
        return module;
    }

    private static org.gradle.api.NamedDomainObjectCollection<MavenPublication> publications(Project project) {
        return requireExtension(project, PublishingExtension.class).getPublications().withType(MavenPublication.class);
    }
}
