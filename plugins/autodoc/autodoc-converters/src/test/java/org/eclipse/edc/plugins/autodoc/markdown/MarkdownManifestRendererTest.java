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

package org.eclipse.edc.plugins.autodoc.markdown;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.plugins.autodoc.spi.ManifestWriter;
import org.eclipse.edc.runtime.metamodel.domain.EdcModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownManifestRendererTest {

    private ManifestWriter writer;
    private ByteArrayOutputStream testOutputStream;

    @BeforeEach
    void setUp() {
        testOutputStream = new ByteArrayOutputStream();
        writer = new ManifestWriter(new MarkdownManifestRenderer(testOutputStream));
    }

    @Test
    void convert_exampleJson() {
        var list = generateManifest("example_manifest.json");
        var os = writer.convert(list);

        var result = testOutputStream.toString();
        assertThat(result).isNotNull();
        assertThat(os).isEqualTo(testOutputStream);

        System.out.println(result);
    }

    @Test
    void convert_simpleJson() {
        var list = generateManifest("simple_manifest.json");
        var os = writer.convert(list);

        var result = testOutputStream.toString();
        assertThat(result).isNotNull();
        assertThat(os).isEqualTo(testOutputStream);

        System.out.println(result);
    }

    private List<EdcModule> generateManifest(String filename) {
        try {
            return new ObjectMapper().readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream(filename), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}