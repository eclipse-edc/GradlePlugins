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

package org.eclipse.dataspaceconnector.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Gradle plugin to download an arbitrary artifact from a remote repository
 */
public class AutodocPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().register("autodoc", GenerateDocumentationTask.class);
    }
}
