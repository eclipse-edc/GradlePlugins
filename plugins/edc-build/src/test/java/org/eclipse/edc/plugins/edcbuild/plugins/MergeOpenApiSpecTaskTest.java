/*
 *  Copyright (c) 2026 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.edcbuild.plugins;

import com.sun.net.httpserver.HttpServer;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MergeOpenApiSpecTaskTest {

    @TempDir
    private Path tempDir;
    private Path specs;
    private MergeOpenApiSpecTask task;
    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {
        Project project = ProjectBuilder.builder().withProjectDir(tempDir.resolve("project").toFile()).build();
        project.getExtensions().create("edcBuild", BuildExtension.class);
        project.getPlugins().apply(OpenApiMergerPlugin.class);
        task = (MergeOpenApiSpecTask) project.getTasks().getByName(MergeOpenApiSpecTask.NAME);
        specs = Files.createDirectories(tempDir.resolve("specs"));
    }

    @Test
    void shouldCombineOperationsDeclaredOnTheSamePathByDifferentSpecs() throws IOException {
        Files.writeString(specs.resolve("a-selector.yaml"), spec("get", "getDataPlanes", "DataPlane"));
        Files.writeString(specs.resolve("b-registration.yaml"), spec("put", "registerDataPlane", "Registration"));

        var merged = merge();

        var dataPlanes = merged.getPaths().get("/v4/dataplanes");
        assertThat(dataPlanes.getGet().getOperationId()).isEqualTo("getDataPlanes");
        assertThat(dataPlanes.getPut().getOperationId()).isEqualTo("registerDataPlane");
        assertThat(merged.getComponents().getSchemas()).containsKeys("DataPlane", "Registration");
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldFail_whenExternalReferenceCannotBeFetched() throws IOException {
        Files.writeString(specs.resolve("a-selector.yaml"), spec("get", "getDataPlanes", "DataPlane"));
        Files.writeString(specs.resolve("b-registration.yaml"), specWithExternalRef("put", "registerDataPlane", "http://localhost:%d/schema.json".formatted(unusedPort())));

        assertThatThrownBy(this::merge)
                .isInstanceOf(GradleException.class)
                .hasMessageContaining("b-registration.yaml")
                .hasMessageContaining("PUT /v4/dataplanes")
                .hasMessageNotContaining("a-selector.yaml");
    }

    @Test
    void shouldRetry_whenExternalReferenceFetchFailsTransiently() throws IOException {
        var requests = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/schema.json", exchange -> {
            if (requests.incrementAndGet() == 1) {
                exchange.sendResponseHeaders(503, -1);
            } else {
                var body = "{\"type\": \"object\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            }
            exchange.close();
        });
        server.start();
        Files.writeString(specs.resolve("a-selector.yaml"), spec("get", "getDataPlanes", "DataPlane"));
        Files.writeString(specs.resolve("b-registration.yaml"), specWithExternalRef("put", "registerDataPlane", "http://localhost:%d/schema.json".formatted(server.getAddress().getPort())));

        var merged = merge();

        var dataPlanes = merged.getPaths().get("/v4/dataplanes");
        assertThat(dataPlanes.getGet()).isNotNull();
        assertThat(dataPlanes.getPut()).isNotNull();
        assertThat(requests).hasValueGreaterThan(1);
    }

    @Test
    void shouldSetInfo() throws IOException {
        Files.writeString(specs.resolve("selector.yaml"), spec("get", "getDataPlanes", "DataPlane"));
        task.setInfoTitle("management-api");
        task.setInfoDescription("REST API documentation for the management-api");
        task.setInfoVersion("4.0.0");

        var merged = merge();

        assertThat(merged.getInfo().getTitle()).isEqualTo("management-api");
        assertThat(merged.getInfo().getDescription()).isEqualTo("REST API documentation for the management-api");
        assertThat(merged.getInfo().getVersion()).isEqualTo("4.0.0");
        assertThat(merged.getInfo().getLicense().getName()).isEqualTo("The Apache License, Version 2.0");
    }

    private OpenAPI merge() throws IOException {
        var output = tempDir.resolve("out").resolve("merged.yaml");
        task.setInputDir(specs.toString());
        task.setOutput(output.toString());

        task.task();

        return new OpenAPIParser().readLocation(output.toString(), emptyList(), new ParseOptions()).getOpenAPI();
    }

    private int unusedPort() throws IOException {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private String specWithExternalRef(String method, String operationId, String ref) {
        return """
                openapi: 3.0.1
                paths:
                  /v4/dataplanes:
                    %s:
                      operationId: %s
                      responses:
                        "200":
                          description: ok
                          content:
                            application/json:
                              schema:
                                $ref: '%s'
                """.formatted(method, operationId, ref);
    }

    private String spec(String method, String operationId, String schema) {
        return """
                openapi: 3.0.1
                paths:
                  /v4/dataplanes:
                    %s:
                      operationId: %s
                      responses:
                        "200":
                          description: ok
                          content:
                            application/json:
                              schema:
                                $ref: '#/components/schemas/%s'
                components:
                  schemas:
                    %s:
                      type: object
                """.formatted(method, operationId, schema, schema);
    }
}
