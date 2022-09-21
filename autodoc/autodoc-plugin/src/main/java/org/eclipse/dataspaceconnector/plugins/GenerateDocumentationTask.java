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

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * Enhanced task to download an artifact from a remote repository
 */
public abstract class GenerateDocumentationTask extends DefaultTask {

    /**
     * Generates documentation
     */
    @TaskAction
    public void generateDocumentation() {
        System.out.println("Generating documentation...");
    }

}
