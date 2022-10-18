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

package org.eclipse.dataspaceconnector.plugins.edcbuild.conventions;

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.publish.PublishingExtension;

import static org.eclipse.dataspaceconnector.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

class MavenPublishingConvention implements EdcConvention {
    @Override
    public void apply(Project target) {
        if (target.hasProperty("skip.signing")) {
            return;
        }
        var pubExt = requireExtension(target, PublishingExtension.class);

        if (pubExt.getRepositories().stream().noneMatch(repo -> repo.getName().equals("OSSRH") && repo instanceof MavenArtifactRepository)) {
            pubExt.repositories(handler -> handler.maven(repo -> {
                repo.setUrl("https://oss.sonatype.org/content/repositories/snapshots/");
                repo.setName("OSSRH");
                repo.credentials(PasswordCredentials.class, creds -> {
                    creds.setPassword(System.getenv("OSSRH_PASSWORD"));
                    creds.setUsername(System.getenv("OSSRH_USER"));
                });
            }));
        }
    }

}
