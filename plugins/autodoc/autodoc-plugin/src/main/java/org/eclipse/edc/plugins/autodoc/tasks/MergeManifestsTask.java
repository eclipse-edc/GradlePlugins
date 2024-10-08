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

package org.eclipse.edc.plugins.autodoc.tasks;

import org.eclipse.edc.plugins.autodoc.AutodocExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.internal.GFileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Task that takes an input file (JSON) and appends its contents to a destination file. This task is intended to be called per-project.
 */
public class MergeManifestsTask extends DefaultTask {

    public static final String NAME = "mergeManifests";
    private static final String MERGED_MANIFEST_FILENAME = "manifest.json";
    private final JsonFileAppender appender;
    private File destinationFile;

    public MergeManifestsTask() {
        appender = new JsonFileAppender(getProject().getLogger());
        var rootProjectPath = getProject().getRootProject().getLayout().getBuildDirectory().getAsFile().get().getAbsolutePath();
        destinationFile = Path.of(rootProjectPath, MERGED_MANIFEST_FILENAME).toFile();
    }


    @TaskAction
    public void mergeManifests() {
        var autodocExt = getProject().getExtensions().findByType(AutodocExtension.class);

        Objects.requireNonNull(autodocExt, "AutodocExtension cannot be null");

        var destination = getDestinationFile();

        if (destination == null) {
            throw new GradleException("destinationFile must be configured but was null!");
        }

        var projectBuildDirectory = getProject().getLayout().getBuildDirectory().getAsFile();
        var sourceFile = Path.of(autodocExt.getOutputDirectory().convention(projectBuildDirectory).get().getAbsolutePath(), "edc.json").toFile();
        if (sourceFile.exists()) {
            appender.append(destination, sourceFile);
        } else {
            getProject().getLogger().lifecycle("Skip project [{}] - no manifest file found", sourceFile);
        }

        // if an additional input directory was specified, lets include the files in it.
        if (autodocExt.getAdditionalInputDirectory().isPresent() &&
                autodocExt.getAdditionalInputDirectory().get().exists() &&
                getProject().equals(getProject().getRootProject()) &&
                autodocExt.isIncludeTransitive()) {
            var dir = autodocExt.getAdditionalInputDirectory().get();
            var files = GFileUtils.listFiles(dir, new String[]{ "json" }, false);
            getLogger().lifecycle("Appending [{}] additional JSON files to the merged manifest", files.size());
            files.forEach(f -> appender.append(destination, f));
        }

    }

    /**
     * The destination file. By default, it is set to {@code <rootProject>/build/manifest.json}
     */
    @OutputFile
    public File getDestinationFile() {
        return destinationFile;
    }

    public void setDestinationFile(File destinationFile) {
        this.destinationFile = destinationFile;
    }

}
