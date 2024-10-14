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
import org.gradle.api.tasks.options.Option;
import org.gradle.util.internal.GFileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Task that takes an input file (JSON) and appends its contents to a destination file. This task is intended to be called per-project.
 */
public abstract class MergeManifestsTask extends DefaultTask {

    public static final String NAME = "mergeManifests";
    private static final String MERGED_MANIFEST_FILENAME = "manifest.json";
    private final JsonFileAppender appender;
    private final File projectBuildDirectory;
    private File destinationFile;
    private File inputDirectory;
    private String outputDirectoryOption;

    public MergeManifestsTask() {
        appender = new JsonFileAppender(getProject().getLogger());
        projectBuildDirectory = getProject().getLayout().getBuildDirectory().getAsFile().get();
        destinationFile = getProject().getRootProject().getLayout().getBuildDirectory().get().getAsFile().toPath().resolve(MERGED_MANIFEST_FILENAME).toFile();
        inputDirectory = projectBuildDirectory.toPath().resolve("autodoc").toFile();
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

    @TaskAction
    public void mergeManifests() {
        File destination;
        if (outputDirectoryOption != null) {
            var customOutputDir = new File(outputDirectoryOption);
            customOutputDir.mkdirs();
            destination = new File(customOutputDir, MERGED_MANIFEST_FILENAME);
        } else {
            destination = destinationFile;
        }

        var autodocExt = getProject().getExtensions().findByType(AutodocExtension.class);

        Objects.requireNonNull(autodocExt, "AutodocExtension cannot be null");

        if (destination == null) {
            throw new GradleException("destinationFile must be configured but was null!");
        }

        var sourceFile = Path.of(autodocExt.getOutputDirectory().convention(projectBuildDirectory).get().getAbsolutePath(), "edc.json").toFile();

        if (sourceFile.exists()) {
            appender.append(destination, sourceFile);
        } else {
            getProject().getLogger().lifecycle("Skip project [{}] - no manifest file found", sourceFile);
        }


        // if an additional input directory was specified via CLI, lets include the files in it.
        if (inputDirectory != null &&
                inputDirectory.exists() &&
                autodocExt.isIncludeTransitive()) {
            var files = GFileUtils.listFiles(inputDirectory, new String[]{ "json" }, false);
            getLogger().lifecycle("Appending [{}] additional JSON files from the inputDirectory to the merged manifest", files.size());
            files.forEach(f -> appender.append(destination, f));
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

    @Option(option = "input", description = "Directory where previously downloaded or resolved manifest files reside")
    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = new File(inputDirectory);
    }

    @Option(option = "output", description = "Directory where the merged manifest should be stored")
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectoryOption = outputDirectory;
    }
}
