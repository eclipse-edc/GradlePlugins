/*
 * Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.autodoc;

import org.eclipse.edc.plugins.autodoc.tasks.MarkdownRendererTask;
import org.eclipse.edc.plugins.autodoc.tasks.MergeManifestsTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.List;

/**
 * Gradle plugin that injects an {@code annotationProcessor} dependency to any Gradle project so that the autodoc processor can run during compile.
 */
public class AutodocPlugin implements Plugin<Project> {

    private final List<String> exclusions = List.of("version-catalog", "edc-build", "module-names", "openapi-merger", "test-summary", "autodoc-plugin", "autodoc-processor");

    @Override
    public void apply(Project project) {
        var extension = project.getExtensions().create("autodocextension", AutodocExtension.class);

        if (!exclusions.contains(project.getName())) {
            project.getGradle().addListener(new AutodocDependencyInjector(project, extension));
        }

        // registers a "named" task, that does nothing, except depend on the compileTask, which then runs the annotation processor
        project.getTasks().register("autodoc", t -> t.dependsOn("compileJava"));
        project.getTasks().register("mergeManifest", MergeManifestsTask.class, t -> t.dependsOn("autodoc").finalizedBy("doc2md"));
        project.getTasks().register("doc2md", MarkdownRendererTask.class, t -> t.dependsOn("autodoc"));

    }

}
