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

import org.eclipse.edc.plugins.edcbuild.Versions;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Map.entry;

/**
 * Applies default test dependencies to all "java-library" projects: JUnit, Mockito and AssertJ in their respective
 * configurations.
 */
class DefaultTestDependencyConvention implements EdcConvention {

    @Override
    public void apply(Project target) {
        target.getPluginManager().withPlugin("java-library", plugin -> {
            var dependencyHandler = target.getDependencies();

            var dependencies = Map.of(
                    "Implementation", List.of(
                            dependencyHandler.platform(format("org.junit:junit-bom:%s", Versions.JUPITER)),
                            "org.junit.jupiter:junit-jupiter-api",
                            "org.junit.jupiter:junit-jupiter-params",
                            "org.mockito:mockito-core:%s".formatted(Versions.MOCKITO),
                            "org.assertj:assertj-core:%s".formatted(Versions.ASSERTJ)
                    ),
                    "RuntimeOnly", List.of(
                            "org.junit.platform:junit-platform-launcher",
                            "org.junit.jupiter:junit-jupiter-engine"
                    )
            );

            prepareDependenciesFor(dependencies, "test")
                    .forEach(dependency -> dependencyHandler.add(dependency.getKey(), dependency.getValue()));

            if (target.getPluginManager().hasPlugin("java-test-fixtures")) {
                prepareDependenciesFor(dependencies, "testFixtures")
                        .forEach(dependency -> dependencyHandler.add(dependency.getKey(), dependency.getValue()));
            }
        });
    }

    private @NotNull Stream<? extends Map.Entry<String, ?>> prepareDependenciesFor(Map<@NotNull String, @NotNull List<?>> dependencies, String configurationContext) {
        return dependencies.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(it -> entry(configurationContext + entry.getKey(), it)));
    }

}
