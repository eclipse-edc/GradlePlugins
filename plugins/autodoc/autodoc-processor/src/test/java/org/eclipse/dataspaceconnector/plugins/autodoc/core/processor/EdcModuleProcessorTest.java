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

import org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.testclasses.OptionalService;
import org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.testclasses.RequiredService;
import org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.testclasses.SampleExtensionWithoutAnnotation;
import org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.testclasses.SecondExtension;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.EdcServiceExtension;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.Service;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.ServiceReference;
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
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.TestFunctions.filterManifest;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.TestFunctions.readManifest;

class EdcModuleProcessorTest {

    private static final String EDC_ID = "test-edc-id";
    private static final String EDC_VERSION = "test-edc-version";

    @TempDir
    private Path tempDir;
    private JavaCompiler.CompilationTask task;
    private DiagnosticCollector<JavaFileObject> diagnostics;

    @BeforeEach
    void setUp() throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        diagnostics = new DiagnosticCollector<>();
        var fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(tempDir.toFile()));
        fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, List.of(tempDir.toFile()));

        var file1 = new File("src/test/java/org/eclipse/dataspaceconnector/plugins/autodoc/core/processor/testclasses/SampleExtensionWithoutAnnotation.java");
        var file2 = new File("src/test/java/org/eclipse/dataspaceconnector/plugins/autodoc/core/processor/testclasses/SecondExtension.java");
        var file3 = new File("src/test/java/org/eclipse/dataspaceconnector/plugins/autodoc/core/processor/testclasses/OptionalService.java");
        var file4 = new File("src/test/java/org/eclipse/dataspaceconnector/plugins/autodoc/core/processor/testclasses/RequiredService.java");
        var compilationUnits = fileManager.getJavaFileObjects(file1, file2, file3, file4);

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

    @Test
    void verifyManifestContainsExtension() {
        task.call();

        var modules = readManifest(filterManifest(tempDir));
        assertThat(modules).hasSize(1);
        assertThat(modules.get(0).getExtensions())
                .hasSize(2)
                .extracting(EdcServiceExtension::getName)
                .containsOnly(SampleExtensionWithoutAnnotation.class.getSimpleName(),
                        SecondExtension.class.getSimpleName());
    }

    @Test
    void verifyManifestContainsCorrectElements() {
        task.call();

        var modules = readManifest(filterManifest(tempDir));
        assertThat(modules).hasSize(1);
        var extensions = modules.get(0).getExtensions();

        assertThat(extensions)
                .allSatisfy(e -> assertThat(e.getCategories()).isNotNull().isEmpty())
                .allSatisfy(e -> assertThat(e.getOverview()).isNull())
                .extracting(EdcServiceExtension::getName)
                .containsOnly(SampleExtensionWithoutAnnotation.class.getSimpleName(), SecondExtension.class.getSimpleName());

        var ext1 = extensions.stream().filter(e -> e.getName().equals(SampleExtensionWithoutAnnotation.class.getSimpleName()))
                .findFirst()
                .orElseThrow();

        var providedServices = ext1.getProvides();
        assertThat(providedServices).hasSize(2)
                .extracting(Service::getService)
                .containsExactlyInAnyOrder(SomeService.class.getName(), SomeOtherService.class.getName());

        var references = ext1.getReferences();
        assertThat(references.size()).isEqualTo(2);
        assertThat(references).contains(new ServiceReference(OptionalService.class.getName(), false));
        assertThat(references).contains(new ServiceReference(RequiredService.class.getName(), true));

        var configuration = ext1.getConfiguration().get(0);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getKey()).isEqualTo(SampleExtensionWithoutAnnotation.TEST_SETTING);
        assertThat(configuration.isRequired()).isTrue();
        assertThat(configuration.getDescription()).isNotEmpty();

        var ext2 = extensions.stream().filter(e -> e.getName().equals(SecondExtension.class.getSimpleName()))
                .findFirst()
                .orElseThrow();

        assertThat(ext2.getProvides()).isEmpty();

        assertThat(ext2.getReferences())
                .hasSize(1)
                .containsOnly(new ServiceReference(SomeService.class.getName(), true));

        assertThat(ext2.getConfiguration()).isEmpty();
    }


}