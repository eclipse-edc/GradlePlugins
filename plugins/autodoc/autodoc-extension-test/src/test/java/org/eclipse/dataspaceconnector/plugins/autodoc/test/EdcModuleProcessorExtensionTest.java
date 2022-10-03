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

package org.eclipse.dataspaceconnector.plugins.autodoc.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.EdcModule;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.ServiceReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the sample extension module is introspected correctly.
 */
class EdcModuleProcessorExtensionTest {

    private static final TypeReference<List<EdcModule>> TYPE_REFERENCE = new TypeReference<>() {
    };
    private URL manifestFileUrl;

    @BeforeEach
    void setup() throws MalformedURLException {
        var userDir = System.getProperty("user.dir"); //will point to the module's directory
        var file = userDir + File.separator + "build" + File.separator + "edc.json";
        manifestFileUrl = Paths.get(file).toUri().toURL();
        assertThat(manifestFileUrl).isNotNull();
    }

    @Test
    void verifyAnnotatedExtension() throws IOException {

        try (var stream = manifestFileUrl.openStream()) {
            var manifests = new ObjectMapper().readValue(stream, TYPE_REFERENCE);
            assertThat(manifests.size()).isEqualTo(1);

            var manifest = manifests.get(0);
            assertThat(manifest.getName()).isEqualTo(SampleExtension.NAME);
            assertThat(manifest.getCategories()).contains(SampleExtension.CATEGORY);
            assertThat(manifest.getOverview()).isNotEmpty();

            var provides = manifest.getProvides();
            assertThat(provides.size()).isEqualTo(1);
            assertThat(provides.get(0).getService()).isEqualTo(ProvidedService1.class.getName());

            var references = manifest.getReferences();
            assertThat(references.size()).isEqualTo(2);
            assertThat(references).contains(new ServiceReference(OptionalService.class.getName(), false));
            assertThat(references).contains(new ServiceReference(RequiredService.class.getName(), true));

            var configuration = manifest.getConfiguration().get(0);
            assertThat(configuration).isNotNull();
            assertThat(configuration.getKey()).isEqualTo(SampleExtension.CONFIG1);
            assertThat(configuration.isRequired()).isTrue();
            assertThat(configuration.getDescription()).isNotEmpty();
        }
    }

}
