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

import io.github.gradlenexus.publishplugin.NexusPublishExtension;
import io.github.gradlenexus.publishplugin.NexusRepository;
import org.gradle.api.Project;

import java.net.URI;

class NexusPublishingConvention implements EdcConvention {

    private static final String NEXUS_USERNAME_ENV_VAR = "CENTRAL_SONATYPE_TOKEN_USERNAME";
    private static final String NEXUS_PASSWORD_ENV_VAR = "CENTRAL_SONATYPE_TOKEN_PASSWORD";

    @Override
    public void apply(Project target) {
        if (target == target.getRootProject()) {

            target.getExtensions().configure(NexusPublishExtension.class, nexusPublishExtension -> {
                nexusPublishExtension.repositories(c -> c.sonatype(this::configure));
            });
        }
    }

    private void configure(NexusRepository r) {
        r.getNexusUrl().set(URI.create(Repositories.NEXUS_REPO_URL));
        r.getSnapshotRepositoryUrl().set(URI.create(Repositories.SNAPSHOT_REPO_URL));
        r.getUsername().set(System.getenv(NEXUS_USERNAME_ENV_VAR));
        r.getPassword().set(System.getenv(NEXUS_PASSWORD_ENV_VAR));
    }


}
