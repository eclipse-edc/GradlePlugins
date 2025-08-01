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
 *       Fraunhofer Institute for Software and Systems Engineering - add maven publication
 *
 */

package org.eclipse.edc.plugins.edcbuild;

import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven;
import org.gradle.plugins.signing.Sign;

import static java.util.List.of;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.allDependencies;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.checkstyle;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.defaultDependencies;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.jar;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.java;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.mavenPom;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.mavenPublication;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.mavenPublishing;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.printClasspath;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.repositories;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.rootBuildScript;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.signing;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.swagger;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.tests;
import static org.eclipse.edc.plugins.edcbuild.conventions.Conventions.waitForPublishedArtifacts;

/**
 * Adds (opinionated) conventions (=configuration) for various plugins.
 */
public class EdcBuildPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {

        // register the extension(s)
        target.getExtensions().create("edcBuild", BuildExtension.class, target.getObjects());

        // apply all plugins
        target.getPlugins().apply(EdcBuildBasePlugin.class);

        target.getTasks().withType(AbstractPublishToMaven.class).configureEach(task -> {
            var signTasks = target.getTasks().withType(Sign.class);
            task.mustRunAfter(signTasks);
        });

        // configuration values are only guaranteed to be set after the project has been evaluated
        // https://docs.gradle.org/current/userguide/build_lifecycle.html
        target.afterEvaluate(project -> {
            if (project.getState().getFailure() != null) {
                return;
            }

            // apply the conventions
            of(
                    rootBuildScript(),
                    java(),
                    repositories(),
                    defaultDependencies(),
                    checkstyle(),
                    mavenPublishing(),
                    mavenPublication(),
                    signing(),
                    mavenPom(),
                    allDependencies(),
                    tests(),
                    jar(),
                    swagger(),
                    printClasspath(),
                    waitForPublishedArtifacts()
            ).forEach(c -> c.apply(project));
        });


    }
}
