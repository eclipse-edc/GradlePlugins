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
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.List;

/**
 * Gradle plugin to download an arbitrary artifact from a remote repository
 */
public class AutodocPlugin implements Plugin<Project> {
    private static final String ANNOTATION_PROCESSOR = "annotationProcessor";
    private static final String VERSION = "org.eclipse.dataspaceconnector:autodoc-processor:0.0.1-SNAPSHOT";

    @Override
    public void apply(Project project) {
        project.getGradle().addListener(new DependencyResolutionListener() {
            @Override
            public void beforeResolve(ResolvableDependencies dependencies) {

                if (addAnnotationProcessorDependency(project)) {
                    var task = (JavaCompile) project.getTasks().findByName("compileJava");
                    task.getOptions().getCompilerArgs().addAll(List.of("-Aedc.id=foobarbaz", "-Aedc.version=yomama"));
                }
                project.getGradle().removeListener(this);

            }

            @Override
            public void afterResolve(ResolvableDependencies dependencies) {

            }
        });

    }

    private boolean addAnnotationProcessorDependency(Project project) {
        var apConfig = project.getConfigurations().findByName(ANNOTATION_PROCESSOR);
        if (apConfig != null) {
            apConfig.getDependencies().add(project.getDependencies().create(VERSION));
        }
        return apConfig != null;
    }
}
