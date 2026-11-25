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
import io.swagger.v3.oas.models.OpenAPI;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
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
 * <p>
 * The merge resolves external references (e.g. JSON schemas published on w3id.org), and a spec whose references can't
 * be fetched is skipped by the merger with just a log line. So the merged spec is checked to contain every operation of
 * every input spec: the merge is attempted again a few times, as such failures are usually transient, and the task
 * fails if operations are still missing.
 */
@DisableCachingByDefault(because = "the input directory is passed in via command line")
public abstract class MergeOpenApiSpecTask extends DefaultTask {

    public static final String NAME = "mergeOpenApiSpec";

    private static final List<String> SPEC_EXTENSIONS = List.of(".yaml", ".yml", ".json");
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MILLIS = 2000;

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

        var specFiles = specFiles();
        var expectedOperations = operationsBySpec(specFiles);

        OpenAPI openApi = null;
        Map<String, Set<String>> missingOperations = Map.of();
        for (var attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            if (attempt > 1) {
                getLogger().warn("Merged OpenAPI spec misses the operations of {}, attempt {} of {}", missingOperations.keySet(), attempt, MAX_ATTEMPTS);
                pause(attempt);
            }
            openApi = merge(specFiles);
            missingOperations = missingOperations(expectedOperations, operations(openApi));
            if (missingOperations.isEmpty()) {
                break;
            }
        }

        if (!missingOperations.isEmpty()) {
            var details = missingOperations.entrySet().stream()
                    .map(entry -> "%s: %s".formatted(entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(System.lineSeparator()));
            throw new GradleException("The merged OpenAPI spec misses operations after %d attempts, the specs could not be read (e.g. an external reference could not be fetched):%n%s"
                    .formatted(MAX_ATTEMPTS, details));
        }

        var info = new Info();
        info.setTitle(infoTitle);
        info.setDescription(infoDescription);
        info.setVersion(infoVersion);
        info.setLicense(createLicense());
        openApi.setInfo(info);

        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, Yaml.pretty(openApi));
    }

    private OpenAPI merge(List<String> specFiles) {
        var merged = new MergedSpecBuilder(specFiles, workDir.toString(), "merged", infoTitle, infoDescription, infoVersion, null)
                .withMergeMode(DEEP)
                .withConflictStrategy(WARN)
                .buildMergedSpec();

        return parse(merged);
    }

    /**
     * Collects the operations declared by each spec. References are not resolved, so this reads local files only.
     */
    private Map<String, Set<String>> operationsBySpec(List<String> specFiles) {
        var operations = new LinkedHashMap<String, Set<String>>();
        specFiles.forEach(file -> operations.put(file, operations(parse(file))));
        return operations;
    }

    private OpenAPI parse(String location) {
        var options = new ParseOptions();
        var result = new OpenAPIParser().readLocation(location, Collections.emptyList(), options);
        if (result.getOpenAPI() == null) {
            throw new GradleException("Cannot read OpenAPI spec %s: %s".formatted(location, result.getMessages()));
        }
        return result.getOpenAPI();
    }

    private Set<String> operations(OpenAPI openApi) {
        var operations = new TreeSet<String>();
        if (openApi.getPaths() != null) {
            openApi.getPaths().forEach((path, item) -> item.readOperationsMap().keySet()
                    .forEach(method -> operations.add("%s %s".formatted(method, path))));
        }
        return operations;
    }

    private Map<String, Set<String>> missingOperations(Map<String, Set<String>> expected, Set<String> actual) {
        var missing = new LinkedHashMap<String, Set<String>>();
        expected.forEach((file, operations) -> {
            var notMerged = operations.stream().filter(operation -> !actual.contains(operation)).collect(Collectors.toCollection(TreeSet::new));
            if (!notMerged.isEmpty()) {
                missing.put(Path.of(file).getFileName().toString(), notMerged);
            }
        });
        return missing;
    }

    private void pause(int attempt) {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS * (attempt - 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GradleException("Interrupted while merging the OpenAPI specs", e);
        }
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
