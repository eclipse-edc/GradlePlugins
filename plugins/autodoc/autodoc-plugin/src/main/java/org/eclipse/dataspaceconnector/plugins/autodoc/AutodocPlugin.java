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

package org.eclipse.dataspaceconnector.plugins.autodoc;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Gradle plugin to download an arbitrary artifact from a remote repository
 */
public class AutodocPlugin implements Plugin<Project> {

    // todo: could this get somehow resolved automatically?
    private static final String DEPENDENCY_NAME = "org.eclipse.dataspaceconnector:autodoc-processor:0.0.1-SNAPSHOT";

    @Override
    public void apply(Project project) {
        // adds the annotation processor dependency
        project.getGradle().addListener(new AutodocDependencyAdder(project, DEPENDENCY_NAME));
        // registers a "named" task, that does nothing, except depend on the compileTask, which then runs the annotation processor
        project.getTasks().register("autodoc", t -> t.dependsOn("compileJava"));
    }

}
