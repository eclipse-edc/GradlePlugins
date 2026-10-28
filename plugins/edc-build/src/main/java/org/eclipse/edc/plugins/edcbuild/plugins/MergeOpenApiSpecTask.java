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

package org.eclipse.edc.plugins.edcbuild.plugins;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.gradle.work.DisableCachingByDefault;
import org.openapitools.codegen.config.MergedSpecBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;
import static org.openapitools.codegen.config.MergedSpecBuilder.MergeConflictStrategy.WARN;
import static org.openapitools.codegen.config.MergedSpecBuilder.MergeMode.DEEP;

/**
 * Merges the partial OpenAPI specs found in a directory into a single spec, with the input and output passed in via
 * command line.
 * <p>
 * The specs are merged in file name order, operations declared on the same path by different specs are combined (e.g.
 * GET from one module and PUT from another), and a conflicting definition (same component name, or same method on the
 * same path) keeps the first one, with a warning.
 */
@DisableCachingByDefault(because = "the input directory is passed in via command line")
public abstract class MergeOpenApiSpecTask extends DefaultTask {

    public static final String NAME = "mergeOpenApiSpec";

    private static final List<String> SPEC_EXTENSIONS = List.of(".yaml", ".yml", ".json");

    private Path inputDir;
    private Path workDir = getProject().getLayout().getBuildDirectory().get().dir("openapi").dir("generated").getAsFile().toPath();
    private Path output = getProject().getLayout().getBuildDirectory().get().dir("openapi").file("merged.yaml").getAsFile().toPath();
    private String infoTitle = getProject().getName();
    private String infoDescription = "API documentation";
    private String infoVersion = "0.0.0";

    @Option(option = "inputDir", description = "Input directory where to look for partial specs. Required")
    public void setInputDir(String inputDir) {
        this.inputDir = Path.of(inputDir);
    }

    @Option(option = "outputDir", description = "Working directory for the intermediate merged spec. Optional.")
    public void setOutputDir(String outputDirectory) {
        this.workDir = Path.of(outputDirectory);
    }

    @Option(option = "output", description = "Output merged file path")
    public void setOutput(String output) {
        this.output = Path.of(output);
    }

    @Option(option = "infoTitle", description = "OpenAPI info.title value")
    public void setInfoTitle(String infoTitle) {
        this.infoTitle = infoTitle;
    }

    @Option(option = "infoDescription", description = "OpenAPI info.description value")
    public void setInfoDescription(String infoDescription) {
        this.infoDescription = infoDescription;
    }

    @Option(option = "infoVersion", description = "OpenAPI info.version value")
    public void setInfoVersion(String infoVersion) {
        this.infoVersion = infoVersion;
    }

    @TaskAction
    public void task() throws IOException {
        if (inputDir == null) {
            throw new GradleException("The --inputDir option is required");
        }

        var merged = new MergedSpecBuilder(specFiles(), workDir.toString(), "merged", infoTitle, infoDescription, infoVersion, null)
                .withMergeMode(DEEP)
                .withConflictStrategy(WARN)
                .buildMergedSpec();

        var openApi = new OpenAPIParser().readLocation(merged, Collections.emptyList(), new ParseOptions()).getOpenAPI();
        var info = new Info();
        info.setTitle(infoTitle);
        info.setDescription(infoDescription);
        info.setVersion(infoVersion);
        info.setLicense(createLicense());
        openApi.setInfo(info);

        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, Yaml.pretty(openApi));
    }

    private List<String> specFiles() throws IOException {
        try (Stream<Path> files = Files.walk(inputDir)) {
            return files.filter(Files::isRegularFile)
                    .filter(file -> SPEC_EXTENSIONS.stream().anyMatch(extension -> file.toString().endsWith(extension)))
                    .map(file -> file.toAbsolutePath().toString())
                    .sorted()
                    .toList();
        }
    }

    private License createLicense() {
        var pom = requireExtension(getProject(), BuildExtension.class).getPom();
        var license = new License();
        license.setName(pom.getLicenseName().get());
        license.setUrl(pom.getLicenseUrl().get());
        return license;
    }

}
