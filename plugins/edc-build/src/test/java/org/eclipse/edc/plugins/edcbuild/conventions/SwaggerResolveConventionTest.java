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
import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.plugins.edcbuild.conventions.SwaggerResolveConvention.SWAGGER_GRADLE_PLUGIN;

class SwaggerResolveConventionTest {

    private static final String PROJECT_NAME = "testproject";
    private final Project project = ProjectBuilder.builder().withName(PROJECT_NAME).build();
    private final SwaggerResolveConvention convention = new SwaggerResolveConvention();

    @BeforeEach
    void setUp() {
        project.getRepositories().mavenCentral();
        project.getPluginManager().apply(SWAGGER_GRADLE_PLUGIN);
        project.getPluginManager().apply(JavaPlugin.class);
        project.getExtensions().create("edcBuild", BuildExtension.class, project.getObjects());
    }

    @Test
    void shouldDisableTask_whenNoGroupSpecified() {
        convention.apply(project);

        var resolveTask = (ResolveTask) project.getTasks().getByName("resolve");

        assertThat(resolveTask.isEnabled()).isFalse();
        assertThat(resolveTask.getDependsOn()).hasSize(0);
    }

    @Test
    void shouldCreateTaskForApiGroup() {
        var swagger = ConventionFunctions.requireExtension(project, BuildExtension.class).getSwagger();
        swagger.apiGroup("test-api");

        convention.apply(project);
        ((ProjectInternal) project).evaluate();
        var resolveTask = project.getTasks().getByName("resolve");

        assertThat(resolveTask.getDependsOn()).hasSize(1).first().isInstanceOfSatisfying(TaskProvider.class, taskProvider -> {
            assertThat(taskProvider.isPresent()).isTrue();
            assertThat(taskProvider.get()).isInstanceOfSatisfying(ResolveTask.class, actual -> {
                assertThat(actual.getOutputDir().get().toString()).endsWith("/resources/openapi/yaml/test-api");
                assertThat(actual.getOutputFileName().get()).isEqualTo(PROJECT_NAME);
                assertThat(actual.getOutputFormat().get()).isEqualTo(ResolveTask.Format.YAML);
                assertThat(actual.getResourcePackages().get()).containsExactly("org.eclipse.edc");
            });
        });
    }

    @Test
    void shouldAssignPackageNames() {
        var swagger = ConventionFunctions.requireExtension(project, BuildExtension.class).getSwagger();
        swagger.apiGroup("test-api", "package.name");

        convention.apply(project);
        ((ProjectInternal) project).evaluate();
        var resolveTask = project.getTasks().getByName("resolve");

        assertThat(resolveTask.getDependsOn()).hasSize(1).first().isInstanceOfSatisfying(TaskProvider.class, taskProvider -> {
            assertThat(taskProvider.isPresent()).isTrue();
            assertThat(taskProvider.get()).isInstanceOfSatisfying(ResolveTask.class, actual -> {
                assertThat(actual.getOutputDir().get().toString()).endsWith("/resources/openapi/yaml/test-api");
                assertThat(actual.getOutputFileName().get()).isEqualTo(PROJECT_NAME);
                assertThat(actual.getOutputFormat().get()).isEqualTo(ResolveTask.Format.YAML);
                assertThat(actual.getResourcePackages().get()).containsExactly("package.name");
            });
        });
    }

    @Test
    void apply_whenOutputDirSet_shouldAppend() {
        var swagger = ConventionFunctions.requireExtension(project, BuildExtension.class).getSwagger();
        swagger.apiGroup("test-api");
        swagger.getOutputDirectory().set(new File("some/funny/path"));

        convention.apply(project);
        ((ProjectInternal) project).evaluate();
        var resolveTask = project.getTasks().getByName("resolve");

        assertThat(resolveTask.getDependsOn()).hasSize(1).first().isInstanceOfSatisfying(TaskProvider.class, taskProvider -> {
            assertThat(taskProvider.isPresent()).isTrue();
            assertThat(taskProvider.get()).isInstanceOfSatisfying(ResolveTask.class, actual -> {
                assertThat(actual.getOutputDir().get().toString()).endsWith("/some/funny/path/test-api");
                assertThat(actual.getOutputFileName().get()).isEqualTo(PROJECT_NAME);
                assertThat(actual.getOutputFormat().get()).isEqualTo(ResolveTask.Format.YAML);
            });
        });
    }

    @Test
    void shouldSetPackage_whenSpecified() {
        var swagger = ConventionFunctions.requireExtension(project, BuildExtension.class).getSwagger();
        swagger.apiGroup("group", "the.package.name", "another.package.name");

        convention.apply(project);
        ((ProjectInternal) project).evaluate();
        var resolveTask = project.getTasks().getByName("resolve");

        assertThat(resolveTask.getDependsOn()).hasSize(1).map(TaskProvider.class::cast).map(Provider::get).map(ResolveTask.class::cast)
                .first().satisfies(task -> {
                    assertThat(task.getResourcePackages().get()).containsExactlyInAnyOrder("the.package.name", "another.package.name");
                });
    }

    @Test
    void shouldRegisterMultipleTasks_whenMultipleApiGroups() {
        var swagger = ConventionFunctions.requireExtension(project, BuildExtension.class).getSwagger();
        swagger.apiGroup("group-one");
        swagger.apiGroup("group-two");

        convention.apply(project);
        ((ProjectInternal) project).evaluate();
        var resolveTask = project.getTasks().getByName("resolve");

        assertThat(resolveTask.getDependsOn()).hasSize(2).map(TaskProvider.class::cast).map(Provider::get).map(ResolveTask.class::cast)
                .anySatisfy(p -> assertThat(p.getOutputDir().get().toString()).endsWith("/resources/openapi/yaml/group-one"))
                .anySatisfy(p -> assertThat(p.getOutputDir().get().toString()).endsWith("/resources/openapi/yaml/group-two"));
    }

    @Deprecated(since = "1.5.0")
    @Test
    void shouldApplyDeprecatedApiGroupCall_whenItIsDefined() {
        var swagger = ConventionFunctions.requireExtension(project, BuildExtension.class).getSwagger();
        swagger.getApiGroup().set("test-api"); // deprecated call
        swagger.apiGroup("another-one");
        swagger.getOutputDirectory().set(new File("some/funny/path"));

        convention.apply(project);
        ((ProjectInternal) project).evaluate();
        var resolveTask = project.getTasks().getByName("resolve");

        assertThat(resolveTask.getDependsOn()).hasSize(1).first().isInstanceOfSatisfying(TaskProvider.class, taskProvider -> {
            assertThat(taskProvider.isPresent()).isTrue();
            assertThat(taskProvider.get()).isInstanceOfSatisfying(ResolveTask.class, actual -> {
                assertThat(actual.getOutputDir().get().toString()).endsWith("/some/funny/path/test-api");
                assertThat(actual.getOutputFileName().get()).isEqualTo(PROJECT_NAME);
                assertThat(actual.getOutputFormat().get()).isEqualTo(ResolveTask.Format.YAML);
            });
        });

    }
}
