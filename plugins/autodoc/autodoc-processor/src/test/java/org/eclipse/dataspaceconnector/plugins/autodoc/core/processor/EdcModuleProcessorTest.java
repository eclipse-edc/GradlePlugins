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

package org.eclipse.dataspaceconnector.plugins.autodoc.core.processor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import static org.assertj.core.api.Assertions.assertThat;

abstract class EdcModuleProcessorTest {

    protected static final String EDC_ID = "test-edc-id";
    protected static final String EDC_VERSION = "test-edc-version";

    @TempDir
    protected Path tempDir;
    protected JavaCompiler.CompilationTask task;
    private DiagnosticCollector<JavaFileObject> diagnostics;

    @BeforeEach
    void setUp() throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        diagnostics = new DiagnosticCollector<>();
        var fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(tempDir.toFile()));
        fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, List.of(tempDir.toFile()));


        var compilationUnits = fileManager.getJavaFileObjects(getCompilationUnits().toArray(File[]::new));

        var options = List.of("-Aedc.id=" + EDC_ID, "-Aedc.version=" + EDC_VERSION);
        task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

        task.setProcessors(Collections.singletonList(new EdcModuleProcessor()));
    }

    @AfterEach
    void tearDown() {
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


    protected abstract List<File> getCompilationUnits();

}