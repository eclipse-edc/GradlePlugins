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

import org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.EdcModuleProcessor;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.List;

import static java.lang.String.format;

/**
 * adds an {@code annotationProcessor("...")} dependency to the project
 */
class AutodocDependencyAdder implements DependencyResolutionListener {
    private static final String ANNOTATION_PROCESSOR = "annotationProcessor";
    private final Project project;
    private final String dependencyName;

    AutodocDependencyAdder(Project project, String dependencyName) {
        this.project = project;
        this.dependencyName = dependencyName;
    }

    @Override
    public void beforeResolve(ResolvableDependencies dependencies) {
        if (addAnnotationProcessorDependency(project)) {
            var task = project.getTasks().findByName("compileJava");
            if ((task instanceof JavaCompile)) {
                var compileJava = (JavaCompile) task;
                var versionArg = format("-A%s=%s", EdcModuleProcessor.VERSION, project.getVersion());
                var idArg = format("-A%s=%s:%s", EdcModuleProcessor.ID, project.getGroup(), project.getName());

                compileJava.getOptions().getCompilerArgs().addAll(List.of(idArg, versionArg));
            }
        }
        project.getGradle().removeListener(this);
    }

    @Override
    public void afterResolve(ResolvableDependencies dependencies) {

    }

    private boolean addAnnotationProcessorDependency(Project project) {
        var apConfig = project.getConfigurations().findByName(ANNOTATION_PROCESSOR);
        if (apConfig != null) {
            apConfig.getDependencies().add(project.getDependencies().create(dependencyName));
        }
        return apConfig != null;
    }
}
