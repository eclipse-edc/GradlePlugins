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

package org.eclipse.edc.plugins.edcbuild;

import org.eclipse.edc.plugins.edcbuild.plugins.ModuleNamesPlugin;
import org.eclipse.edc.plugins.edcbuild.plugins.OpenApiMergerPlugin;
import org.eclipse.edc.plugins.edcbuild.plugins.TestSummaryPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.crypto.checksum.ChecksumPlugin;

/**
 * Defines the capabilities of the EDC build as specified in the Gradle Documentation
 *
 * @see <a href="https://docs.gradle.org/current/userguide/designing_gradle_plugins.html">Gradle Documentation</a>
 */
public class EdcBuildBasePlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        var plugins = target.getPlugins();

        plugins.apply(JavaLibraryPlugin.class);
        plugins.apply(CheckstylePlugin.class);
        plugins.apply(MavenPublishPlugin.class);
        plugins.apply(JavaPlugin.class);
        plugins.apply(TestSummaryPlugin.class);
        plugins.apply(com.vanniktech.maven.publish.MavenPublishPlugin.class);

        if (target == target.getRootProject()) {
            plugins.apply(ChecksumPlugin.class);
            // The nexus publish plugin MUST be applied to the root project only, it'll throw an exception otherwise
            // target.getPlugins().apply(NexusPublishPlugin.class); TODO: here apply the publish plugin
            plugins.apply(OpenApiMergerPlugin.class);
            plugins.apply(ModuleNamesPlugin.class);
        }
    }

}
