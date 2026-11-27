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

import com.vanniktech.maven.publish.DeploymentValidation;
import com.vanniktech.maven.publish.JavadocJar;
import com.vanniktech.maven.publish.MavenPublishBaseExtension;
import com.vanniktech.maven.publish.MavenPublishPlugin;
import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.Project;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

public class MavenPublicationConvention implements EdcConvention {

    @Override
    public void apply(Project target) {
        // do not publish the root project or modules without a build.gradle.kts
        if (target.getRootProject() == target || !target.file("build.gradle.kts").exists()) {
            return;
        }

        var buildExtension = requireExtension(target, BuildExtension.class);

        if (buildExtension.shouldPublish()) {
            if (isSnapshot(target)) {
                // skip javadocs for snapshots. Registered before applying the plugin, so it runs before vanniktech's own
                // afterEvaluate hook, and after the ones registered by other plugins during project evaluation (e.g. shadow)
                target.afterEvaluate(p -> requireExtension(p, MavenPublishBaseExtension.class).configureBasedOnAppliedPlugins(new JavadocJar.None()));
            }

            target.getPlugins().apply(MavenPublishPlugin.class);

            target.getExtensions().configure(MavenPublishBaseExtension.class, extension -> {
                var automaticRelease = true;
                extension.publishToMavenCentral(automaticRelease, DeploymentValidation.NONE);

                extension.signAllPublications();
            });
        }

    }

    private static boolean isSnapshot(Project target) {
        return target.getVersion().toString().endsWith("-SNAPSHOT");
    }

}
