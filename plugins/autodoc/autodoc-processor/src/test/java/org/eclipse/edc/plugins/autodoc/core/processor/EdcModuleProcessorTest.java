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

package org.eclipse.edc.plugins.autodoc.core.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.runtime.metamodel.domain.EdcModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import static org.assertj.core.api.Assertions.assertThat;

abstract class EdcModuleProcessorTest {

    protected static final String EDC_ID = "test-edc-id";
    protected static final String EDC_VERSION = "test-edc-version";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @TempDir
    protected Path tempDir;
    protected JavaCompiler.CompilationTask task;
    protected final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    private final String compilationUnitsPath;

    EdcModuleProcessorTest(String compilationUnitsPath) {
        this.compilationUnitsPath = compilationUnitsPath;
    }

    @BeforeEach
    void setUp() throws IOException {
        task = createTask("-Aedc.id=" + EDC_ID, "-Aedc.version=" + EDC_VERSION, null);
    }

    @ParameterizedTest
    @ArgumentsSource(ValidCompilerArgsProvider.class)
    void verifyOptionalCompilerArgs(String edcId, String edcVersion, String edcOutputDir) throws IOException {
        task = createTask(edcId, edcVersion, edcOutputDir);

        var errorMsg = diagnostics.getDiagnostics().stream().map(Object::toString).collect(Collectors.joining(", "));

        assertThat(task.call()).withFailMessage(errorMsg).isTrue();
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidCompilerArgsProvider.class)
    void verifyRequiredCompilerArgs(String edcId, String edcVersion, String edcOutputDir) throws IOException {
        task = createTask(edcId, edcVersion, edcOutputDir);

        assertThat(task.call()).isFalse();
    }

    @Test
    void verifySuccessfulCompilation() {
        var success = task.call();

        var errorMsg = diagnostics.getDiagnostics().stream().map(Object::toString).collect(Collectors.joining(", "));

        assertThat(success).withFailMessage(errorMsg).isTrue();
    }

    @Test
    void verifyManifestExists() throws IOException {
        task.call();
        assertThat(Files.list(tempDir))
                .withFailMessage("Should contain edc.json")
                .anyMatch(p -> p.getFileName().endsWith("edc.json"));
    }

    protected List<EdcModule> readManifest() {
        try (var files = Files.list(tempDir)) {
            var url = files.filter(p -> p.endsWith("edc.json")).findFirst().orElseThrow(AssertionError::new).toUri().toURL();
            return OBJECT_MAPPER.readValue(url, new TypeReference<>() { });
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private JavaCompiler.CompilationTask createTask(String edcId, String edcVersion, String edcOutputDir) throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();

        var fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(tempDir.toFile()));
        fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, List.of(tempDir.toFile()));

        var compilationUnits = fileManager.getJavaFileObjects(getCompilationUnits().toArray(File[]::new));

        var options = Stream.of(edcId, edcVersion, edcOutputDir).filter(Objects::nonNull).toList();
        var task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
        task.setProcessors(Collections.singletonList(new EdcModuleProcessor()));
        return task;
    }

    private List<File> getCompilationUnits() {
        var f = new File(compilationUnitsPath);
        try (var files = Files.list(f.toPath())) {
            return files.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ValidCompilerArgsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("-Aedc.version=1.2.3", "-Aedc.id=someid", "-Aedc.outputDir=build/some/dir"),
                    Arguments.of("-Aedc.version=1.2.3", "-Aedc.id=someid", null),
                    Arguments.of("-Aedc.version=1.2.3", "-Aedc.id=someid", "-Aedc.outputDir=")
            );
        }
    }

    private static class InvalidCompilerArgsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("-Aedc.version=1.2.3", "-Aedc.id=", "-Aedc.outputDir=some/dir"),
                    Arguments.of("-Aedc.version=1.2.3", null, "-Aedc.outputDir=some/dir"),
                    Arguments.of("-Aedc.version=", "-Aedc.id=someid", "-Aedc.outputDir=some/dir"),
                    Arguments.of(null, "-Aedc.id=someid", "-Aedc.outputDir=some/dir")
            );
        }

    }
}
