/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.autodoc;

import org.eclipse.edc.plugins.autodoc.tasks.AutodocBomTask;
import org.eclipse.edc.plugins.autodoc.tasks.DownloadManifestTask;
import org.eclipse.edc.plugins.autodoc.tasks.MarkdownRendererTask.ToHtml;
import org.eclipse.edc.plugins.autodoc.tasks.MarkdownRendererTask.ToMarkdown;
import org.eclipse.edc.plugins.autodoc.tasks.MergeManifestsTask;
import org.eclipse.edc.plugins.autodoc.tasks.ResolveManifestTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.List;

/**
 * Gradle plugin that injects an {@code annotationProcessor} dependency to any Gradle project so that the autodoc processor can run during compile.
 */
public class AutodocPlugin implements Plugin<Project> {

    public static final String GROUP_NAME = "autodoc";
    public static final String AUTODOC_TASK_NAME = "autodoc";
    private final List<String> exclusions = List.of("version-catalog", "edc-build", "module-names", "openapi-merger", "test-summary", "autodoc-plugin", "autodoc-processor", "autodoc-converters");

    @Override
    public void apply(Project project) {
        var extension = project.getExtensions().create("autodocextension", AutodocExtension.class);

        if (!exclusions.contains(project.getName())) {
            project.getGradle().addListener(new AutodocDependencyInjector(project, extension));
        }

        project.getTasks().register(AUTODOC_TASK_NAME, t -> t.dependsOn("compileJava").setGroup(GROUP_NAME));
        project.getTasks().register(MergeManifestsTask.NAME, MergeManifestsTask.class, t -> t.dependsOn(AUTODOC_TASK_NAME).setGroup(GROUP_NAME));
        project.getTasks().register(ToMarkdown.NAME, ToMarkdown.class, t -> t.setGroup(GROUP_NAME));
        project.getTasks().register(ToHtml.NAME, ToHtml.class, t -> t.setGroup(GROUP_NAME));
        project.getTasks().register(DownloadManifestTask.NAME, DownloadManifestTask.class, t -> t.setGroup(GROUP_NAME));
        // resolving manifests requires the Autodoc manifests of all dependencies to exist already
        project.getTasks().register(ResolveManifestTask.NAME, ResolveManifestTask.class, t -> {
            t.dependsOn(AUTODOC_TASK_NAME);
            t.setGroup(GROUP_NAME);
            t.setDescription(ResolveManifestTask.DESCRIPTION);
        });
        project.getTasks().register(AutodocBomTask.NAME, AutodocBomTask.class, t -> {
            t.dependsOn(ResolveManifestTask.NAME);
            t.setDescription(AutodocBomTask.DESCRIPTION);
            t.setGroup(GROUP_NAME);
        });
    }
}
