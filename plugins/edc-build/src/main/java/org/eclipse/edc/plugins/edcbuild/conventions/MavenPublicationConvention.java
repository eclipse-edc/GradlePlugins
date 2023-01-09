/*
 *  Copyright (c) 2022 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.edcbuild.conventions;

import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Adds a Maven publication to a project if it does not use the application plugin (library module).
 */
public class MavenPublicationConvention implements EdcConvention {
    
    @Override
    public void apply(Project target) {
        var applicationPlugin = target.getPlugins().findPlugin("application");
        if (applicationPlugin == null) {
            var pe = requireExtension(target, PublishingExtension.class);
            pe.publications(publications -> publications.create(target.getName(), MavenPublication.class,
                    mavenPublication -> mavenPublication.from(target.getComponents().getByName("java"))));
        }
    }
    
}
