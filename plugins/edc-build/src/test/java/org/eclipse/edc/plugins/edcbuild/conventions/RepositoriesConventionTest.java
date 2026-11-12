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

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoriesConventionTest {

    private final Project project = ProjectBuilder.builder().build();
    private final RepositoriesConvention convention = new RepositoriesConvention();

    @Test
    void shouldDeclareMavenCentralBeforeSnapshotsRepository() {
        convention.apply(project);

        var urls = project.getRepositories().withType(MavenArtifactRepository.class).stream()
                .map(MavenArtifactRepository::getUrl)
                .map(URI::toString)
                .toList();

        assertThat(urls).hasSize(3);
        assertThat(urls.get(0)).startsWith("file:");
        assertThat(urls.get(1)).isEqualTo("https://repo.maven.apache.org/maven2/");
        assertThat(urls.get(2)).isEqualTo(Repositories.SNAPSHOT_REPO_URL);
    }
}
