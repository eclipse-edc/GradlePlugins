/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.plugins.autodoc.markdown;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.plugins.autodoc.spi.ManifestWriter;
import org.eclipse.edc.runtime.metamodel.domain.ConfigurationSetting;
import org.eclipse.edc.runtime.metamodel.domain.EdcModule;
import org.eclipse.edc.runtime.metamodel.domain.EdcServiceExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownManifestRendererTest {

    private ManifestWriter writer;
    private final ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        writer = new ManifestWriter(new MarkdownManifestRenderer(testOutputStream));
    }

    @Test
    void convert_exampleJson() {
        var list = generateManifest("example_manifest.json");

        var result = writer.convert(list);

        assertThat(result).isNotNull().isEqualTo(testOutputStream);
    }

    @Test
    void convert_simpleJson() {
        var list = generateManifest("simple_manifest.json");

        var result = writer.convert(list);

        assertThat(result).isNotNull().isEqualTo(testOutputStream);
    }

    @Nested
    class Heading {

        @Test
        void shouldRenderHeading() {
            var list = List.of(EdcModule.Builder.newInstance().modulePath("foo:bar").version("0.1.0-baz").build());

            var result = writer.convert(list);

            assertThat(result).isNotNull().isEqualTo(testOutputStream).extracting(Object::toString).satisfies(markdown -> {
                assertThat(markdown).contains("Module `bar`");
                assertThat(markdown).contains("_artifact: _foo:bar:0.1.0-baz");
                assertThat(markdown).contains("### Extension points");
                assertThat(markdown).contains("### Extensions");
                assertThat(markdown).doesNotContain("Configuration:");
            });
        }

        @Test
        void shouldRenderHeading_whenModuleNameIsSet() {
            var list = List.of(EdcModule.Builder.newInstance().name("module name").modulePath("foo:bar").version("0.1.0-baz").build());

            var result = writer.convert(list);

            assertThat(result).isNotNull().isEqualTo(testOutputStream).extracting(Object::toString).satisfies(markdown -> {
                assertThat(markdown).contains("Module `bar`");
                assertThat(markdown).contains("_name: _module name");
                assertThat(markdown).contains("_artifact: _foo:bar:0.1.0-baz");
                assertThat(markdown).contains("### Extension points");
                assertThat(markdown).contains("### Extensions");
                assertThat(markdown).doesNotContain("Configuration:");
            });
        }

    }

    @Nested
    class Configuration {

        @Test
        void shouldRenderConfiguration() {
            var configurationSetting = ConfigurationSetting.Builder.newInstance().key("test.key").build();
            var extension = EdcServiceExtension.Builder.newInstance().name("name").className("className").configuration(List.of(configurationSetting)).build();
            var list = List.of(EdcModule.Builder.newInstance().modulePath("foo").version("0.1.0-bar").extension(extension).build());

            var result = writer.convert(list);

            assertThat(result).isNotNull().extracting(Object::toString).asString()
                    .contains("### Configuration:")
                    .contains("`test.key`");
        }

        @Test
        void shouldRenderDeprecatedConfiguration() {
            var configurationSetting = ConfigurationSetting.Builder.newInstance().key("test.key").deprecated(true).build();
            var extension = EdcServiceExtension.Builder.newInstance().name("name").className("className").configuration(List.of(configurationSetting)).build();
            var list = List.of(EdcModule.Builder.newInstance().modulePath("foo").version("0.1.0-bar").extension(extension).build());

            var result = writer.convert(list);

            assertThat(result).isNotNull().extracting(Object::toString).asString()
                    .contains("### Configuration:")
                    .contains("~~test.key~~");
        }
    }

    private List<EdcModule> generateManifest(String filename) {
        try (var stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
            return new ObjectMapper().readValue(stream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
