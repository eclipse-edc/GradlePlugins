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

package org.eclipse.edc.plugins.edcbuild.conventions;

import io.swagger.v3.plugins.gradle.tasks.ResolveTask;
import org.eclipse.edc.plugins.edcbuild.extensions.ApiGroup;
import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.eclipse.edc.plugins.edcbuild.extensions.SwaggerGeneratorExtension;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginExtension;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

import static org.eclipse.edc.plugins.edcbuild.Versions.JAKARTA_WS_RS;
import static org.eclipse.edc.plugins.edcbuild.Versions.SWAGGER;
import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;
import static org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME;

/**
 * Configures the Swagger Resolve task to create openapi yaml file per project
 */
class SwaggerResolveConvention implements EdcConvention {

    public static final String SWAGGER_GRADLE_PLUGIN = "io.swagger.core.v3.swagger-gradle-plugin";

    public static Path defaultOutputDirectory(Project project) {
        return Path.of(project.getRootProject().getProjectDir().getAbsolutePath(), "resources/openapi/yaml");
    }

    @Override
    public void apply(Project target) {
        target.getPluginManager().withPlugin(SWAGGER_GRADLE_PLUGIN, appliedPlugin -> {

            Stream.of(
                    "io.swagger.core.v3:swagger-jaxrs2-jakarta:%s".formatted(SWAGGER),
                    "jakarta.ws.rs:jakarta.ws.rs-api:%s".formatted(JAKARTA_WS_RS)
            ).forEach(dependency -> target.getDependencies().add(IMPLEMENTATION_CONFIGURATION_NAME, dependency));

            var swaggerExt = requireExtension(target, BuildExtension.class).getSwagger();
            var tasks = target.getTasks();

            var resolve = tasks.named("resolve");
            resolve.configure(it -> it.setEnabled(false));

            var openapiAll = tasks.register("openapi");

            target.afterEvaluate(p -> getApiGroups(swaggerExt).forEach(apiGroup -> {
                var resolveTask = tasks.register("resolve" + apiGroup.name(), ResolveTask.class, task -> {
                    var fallbackOutputDir = defaultOutputDirectory(target);

                    var outputDir = Path.of(swaggerExt.getOutputDirectory().getOrElse(fallbackOutputDir.toFile()).toURI())
                            .resolve(apiGroup.name())
                            .toString();

                    task.setOutputFileName(swaggerExt.getOutputFilename().getOrElse(target.getName()));
                    task.setOutputDir(outputDir);
                    task.setClasspath(getClasspath(target));
                    task.setResourcePackages(apiGroup.packages());

                    baseTaskConfiguration(task, target);
                });
                resolve.configure(t -> t.dependsOn(resolveTask));

                var openapiTask = tasks.register("openapi" + apiGroup.name(), ResolveTask.class, task -> {
                    var outputDir = target.getLayout().getBuildDirectory().getAsFile().get().toPath()
                            .resolve("docs").resolve("openapi")
                            .toString();

                    task.setGroup("documentation");
                    task.setDescription("Generates openapi specification documentation.");
                    task.setOutputFileName(apiGroup.name());
                    task.setResourcePackages(apiGroup.packages());
                    task.setOutputDir(outputDir);

                    baseTaskConfiguration(task, target);
                });
                openapiAll.configure(t -> t.dependsOn(openapiTask));
                tasks.named("jar").configure(t -> t.dependsOn(openapiTask));
            }));

        });
    }

    private static void baseTaskConfiguration(ResolveTask task, Project project) {
        task.setClasspath(getClasspath(project));
        task.setBuildClasspath(task.getClasspath());
        task.setOutputFormat(ResolveTask.Format.YAML);
        task.setSortOutput(true);
        task.setPrettyPrint(true);
        task.setReadAllResources(true);
        task.setSkip(false);
        task.setEncoding("UTF-8");
        task.setAlwaysResolveAppPath(Boolean.FALSE);
        task.setSkipResolveAppPath(Boolean.FALSE);
        task.setOpenAPI31(false);
        task.setConvertToOpenAPI31(false);
    }

    private static @NonNull Set<ApiGroup> getApiGroups(SwaggerGeneratorExtension swaggerExt) {
        return swaggerExt.getApiGroup()
                .map(group -> Set.of(new ApiGroup(group, swaggerExt.getResourcePackages())))
                .getOrElse(swaggerExt.getApiGroups());
    }

    private static @NonNull FileCollection getClasspath(Project target) {
        return requireExtension(target, JavaPluginExtension.class)
                .getSourceSets().getAt("main").getRuntimeClasspath();
    }
}
