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
import org.gradle.api.file.Directory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.jetbrains.annotations.NotNull;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import javax.inject.Inject;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Customization of the {@link GenerateTask}, which allows to pass in the input and output directories via command line.
 */
public class MergeOpenApiSpecTask extends GenerateTask {

    public static final String NAME = "mergeOpenApiSpec";

    private final Directory buildOpenapiFolder = getProject().getLayout().getBuildDirectory().get().dir("openapi");
    private String infoTitle = getProject().getName();
    private String infoDescription = "API documentation";
    private String infoVersion = "0.0.0";
    private Path output = buildOpenapiFolder.file("merged.yaml").getAsFile().toPath();

    @Inject
    public MergeOpenApiSpecTask(@NotNull ObjectFactory objectFactory) {
        super(objectFactory);
        getInputSpecRootDirectorySkipMerge().set(false);
        getSkipValidateSpec().set(true);
        getGeneratorName().set("openapi-yaml");
        getSkipOperationExample().set(false);
        getOutputDir().set(buildOpenapiFolder.dir("generated").getAsFile().getAbsolutePath());
    }

    @Option(option = "inputDir", description = "Input directory where to look for partial specs. Required")
    public void setInputDir(String inputDir) {
        getInputSpecRootDirectory().set(inputDir);
    }

    @Option(option = "outputDir", description = "Output directory where the merged spec file will be stored. Optional.")
    public void setOutputDir(String outputDirectory) {
        getOutputDir().set(outputDirectory);
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
        super.doWork();

        var outputPath = Path.of(getOutputDir().get());
        var spec = outputPath.resolve("openapi").resolve("openapi.yaml");
        var parser = new OpenAPIParser();
        var swaggerParseResult = parser.readLocation(spec.toString(), Collections.emptyList(), new ParseOptions());
        var openApi = swaggerParseResult.getOpenAPI();
        var info = new Info();
        info.setTitle(infoTitle);
        info.setDescription(infoDescription);
        info.setVersion(infoVersion);
        info.setLicense(createLicense());
        openApi.setInfo(info);

        Files.writeString(output, Yaml.pretty(openApi));
    }

    private License createLicense() {
        var pom = requireExtension(getProject(), BuildExtension.class).getPom();
        var license = new License();
        license.setName(pom.getLicenseName().get());
        license.setUrl(pom.getLicenseUrl().get());
        return license;
    }

}

