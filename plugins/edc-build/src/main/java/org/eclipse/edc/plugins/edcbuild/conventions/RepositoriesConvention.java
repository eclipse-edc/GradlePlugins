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
 *
 */

package org.eclipse.edc.plugins.edcbuild.conventions;

import org.gradle.api.Project;

import java.net.URI;

/**
 * Sets default maven repositories for a project
 */
class RepositoriesConvention implements EdcConvention {

    @Override
    public void apply(Project target) {
        var handler = target.getRepositories();
        handler.maven(r -> r.setUrl(URI.create(Repositories.SNAPSHOT_REPO_URL)));
        handler.mavenLocal();
        handler.mavenCentral();
        handler.maven(r -> r.setUrl(URI.create(Repositories.DEPRECATED_SNAPSHOT_REPO_URL)));
    }
}
