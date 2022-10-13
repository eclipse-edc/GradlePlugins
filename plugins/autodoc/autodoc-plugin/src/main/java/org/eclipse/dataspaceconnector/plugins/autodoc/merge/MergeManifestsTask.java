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

package org.eclipse.dataspaceconnector.plugins.autodoc.merge;

import org.eclipse.dataspaceconnector.plugins.autodoc.AutodocExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Task that takes
 */
public class MergeManifestsTask extends DefaultTask {

    private final JsonFileAppender appender;

    public MergeManifestsTask() {
        appender = new JsonFileAppender(getProject().getLogger());
    }

    @TaskAction
    public void mergeManifests() {
        var autodocExt = getProject().getExtensions().findByType(AutodocExtension.class);

        Objects.requireNonNull(autodocExt, "AutodocExtension cannot be null");

        var destination = autodocExt.getDestinationFile().getOrNull();
        var sourceFile = Path.of(autodocExt.getOutputDirectory().get().getAbsolutePath(), "edc.json").toFile();

        if (destination == null) {
            throw new GradleException("destinationFile must be configured but was null!");
        }


        if (sourceFile.exists()) {
            appender.append(destination, sourceFile);
        } else {
            getProject().getLogger().lifecycle("Skip project [{}] - no manifest file found", sourceFile);
        }

    }
}
