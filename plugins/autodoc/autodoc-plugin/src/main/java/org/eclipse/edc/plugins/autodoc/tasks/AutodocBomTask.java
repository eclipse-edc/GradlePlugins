/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.autodoc.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.internal.GFileUtils;

import java.io.File;

public class AutodocBomTask extends DefaultTask {

    public static final String NAME = "autodocBom";
    public static final String DESCRIPTION = """
            This task is intended for BOM modules. It resolves all autodoc manifests of modules that the BOM depends on
            and generates a merged manifest file. By default, this merged file is stored at {project}/build/edc.json.
            """;
    private final JsonFileAppender appender;
    private File outputFile;

    public AutodocBomTask() {
        appender = new JsonFileAppender(getLogger());
        outputFile = getProject().getLayout().getBuildDirectory().file(outputFileName()).get().getAsFile();
    }

    @TaskAction
    public void mergeManifests() {
        if (!getProject().getName().endsWith("-bom")) {
            getLogger().warn("Project name does not end with '-bom'. Is this really a BOM module?");
        }

        var inputDirectory = getProject().getLayout().getBuildDirectory().dir(Constants.DEFAULT_AUTODOC_FOLDER).get();
        if (!inputDirectory.getAsFile().exists()) {
            getLogger().info("Input directory does not exist: {}, Skipping", inputDirectory);
            return;
        }

        var destinationFile = outputFile;

        var files = GFileUtils.listFiles(inputDirectory.getAsFile(), new String[]{ "json" }, false);
        getLogger().debug("Appending [{}] additional JSON files to the merged manifest", files.size());
        files.forEach(f -> appender.append(destinationFile, f));

    }

    @OutputFile
    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    private String outputFileName() {
        return "edc.json";
    }
}
