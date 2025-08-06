/*
 *  Copyright (c) 2022 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V. - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.edcbuild.conventions;

import com.vanniktech.maven.publish.MavenPublishBaseExtension;
import com.vanniktech.maven.publish.MavenPublishPlugin;
import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.Project;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

public class MavenPublicationConvention implements EdcConvention {

    private static final boolean DEFAULT_SHOULD_PUBLISH = true;

    @Override
    public void apply(Project target) {
        // do not publish the root project or modules without a build.gradle.kts
        if (target.getRootProject() == target || !target.file("build.gradle.kts").exists()) {
            return;
        }

        var buildExtension = requireExtension(target, BuildExtension.class);
        var shouldPublish = buildExtension.getPublish().getOrElse(DEFAULT_SHOULD_PUBLISH);

        if (shouldPublish) {
            target.getPlugins().apply(MavenPublishPlugin.class);

            target.getExtensions().configure(MavenPublishBaseExtension.class, extension -> {
                extension.publishToMavenCentral(true);

                extension.signAllPublications();
            });
        }

    }

}
