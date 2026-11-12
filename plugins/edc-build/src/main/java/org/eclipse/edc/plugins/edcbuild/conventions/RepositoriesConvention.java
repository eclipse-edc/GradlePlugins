/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Think-it GmbH - restrict snapshots repository to snapshot artifacts
 *
 */

package org.eclipse.edc.plugins.edcbuild.conventions;

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;

import java.net.URI;

/**
 * Sets default maven repositories for a project.
 * The snapshots repository is restricted to snapshot artifacts only, to avoid looking up every release dependency
 * on it, which leads to rate limiting (HTTP 429) and to the repository being disabled for the rest of the build.
 */
class RepositoriesConvention implements EdcConvention {

    @Override
    public void apply(Project target) {
        var handler = target.getRepositories();
        handler.mavenLocal();
        handler.mavenCentral();
        handler.maven(r -> {
            r.setUrl(URI.create(Repositories.SNAPSHOT_REPO_URL));
            r.mavenContent(MavenRepositoryContentDescriptor::snapshotsOnly);
        });
    }
}
