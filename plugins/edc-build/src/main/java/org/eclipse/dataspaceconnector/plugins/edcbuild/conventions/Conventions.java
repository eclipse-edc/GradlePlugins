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

import static org.eclipse.dataspaceconnector.plugins.edcbuild.conventions.Repositories.SNAPSHOT_REPO_URL;
import static org.eclipse.dataspaceconnector.plugins.edcbuild.conventions.Repositories.mavenRepo;

/**
 * Contains statically accessible {@link EdcConvention} objects that can be applied to a project.
 */
public class Conventions {
    public static EdcConvention checkstyle() {
        return new CheckstyleConvention();
    }


    public static EdcConvention mavenPublishing() {
        return new MavenPublishingConvention();
    }

    public static EdcConvention signing() {
        return new SigningConvention();
    }

    public static EdcConvention repositories() {
        return new RepositoriesConvention();
    }

    public static EdcConvention defaultDependencies() {
        return new DefaultDependencyConvention();
    }

    public static EdcConvention mavenPom() {
        return new MavenArtifactConvention();
    }

    public static EdcConvention jacoco() {
        return new JacocoConvention();
    }

    public static EdcConvention java() {
        return new JavaConvention();
    }

    public static EdcConvention dependencyAnalysis() {
        return new DependencyAnalysisConvention();
    }

    public static EdcConvention tests() {
        return new TestConvention();
    }

    public static EdcConvention jar() {
        return new JarConvention();
    }

    public static EdcConvention nexusPublishing() {
        return new NexusPublishingConvention();
    }

    public static EdcConvention buildscript() {
        return target -> {
            if (target == target.getRootProject()) {
                //configure buildscript repos
                target.getBuildscript().getRepositories().mavenLocal();
                target.getBuildscript().getRepositories().mavenCentral();
                target.getBuildscript().getRepositories().gradlePluginPortal();
                target.getBuildscript().getRepositories().maven(mavenRepo(SNAPSHOT_REPO_URL));
            }
        };
    }
}
