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

package org.eclipse.edc.plugins.modulenames;

import org.eclipse.edc.plugins.openapimerger.OpenApiMergerPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiMergerPluginTest {

    private Project project;

    @BeforeEach
    void setUp() {
        // Create a test project and apply the plugin
        project = ProjectBuilder.builder().build();

        project.getPlugins().apply(OpenApiMergerPlugin.class);
    }

    @Test
    void verify_hasMergerTask() {
        assertThat(project.getTasks().findByName("mergeApiSpec")).isNotNull();
    }


}