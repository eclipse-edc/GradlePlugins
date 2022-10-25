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

package org.eclipse.dataspaceconnector.plugins.edcbuild.conventions;

import org.eclipse.dataspaceconnector.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.compile.JavaCompile;

import static org.eclipse.dataspaceconnector.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Enforces the Java version, generates Javadoc jar and Sources jar for publications
 */
class JavaConvention implements EdcConvention {

    @Override
    public void apply(Project target) {
        var javaPluginExt = requireExtension(target, JavaPluginExtension.class);
        var buildExt = requireExtension(target, BuildExtension.class);
        var javaVersion = buildExt.getJavaLanguageVersion();

        if (javaVersion.isPresent()) {

            // set java version
            javaPluginExt.toolchain(tc -> tc.getLanguageVersion().set(javaVersion));

            // making sure the code does not use any APIs from a more recent version.
            // Ref: https://docs.gradle.org/current/userguide/building_java_projects.html#sec:java_cross_compilation
            target.getTasks().withType(JavaCompile.class, compileTask -> compileTask.getOptions().getRelease().set(javaVersion.get().asInt()));

            target.getTasks().configureEach(t -> {
                var javaCompile = (JavaCompile) t;
                javaCompile.getOptions().setFork(true);
                javaCompile.getOptions().setIncremental(true);
            });
        }


        // needed for publishing to maven central
        javaPluginExt.withJavadocJar();
        javaPluginExt.withSourcesJar();
    }
}
