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

import org.eclipse.edc.plugins.autodoc.core.processor.testspi.ExtensionService;
import org.eclipse.edc.runtime.metamodel.domain.Service;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.plugins.autodoc.core.processor.Constants.TEST_SPI_MODULE;

public class EdcModuleProcessorSpiTest extends EdcModuleProcessorTest {

    EdcModuleProcessorSpiTest() {
        super("src/test/java/org/eclipse/edc/plugins/autodoc/core/processor/testspi/");
    }

    @Test
    void verifyCorrectManifest() {
        task.call();

        var manifest = readManifest();

        assertThat(manifest).hasSize(1);

        var module = manifest.get(0);
        assertThat(module.getName()).isEqualTo(TEST_SPI_MODULE);
        assertThat(module.getVersion()).isEqualTo(EDC_VERSION);
        assertThat(module.getModulePath()).isEqualTo(EDC_ID);
        assertThat(module.getCategories()).hasSize(1).containsOnly("category").isEqualTo(module.getAllCategories());
        assertThat(module.getExtensionPoints()).hasSize(1).containsOnly(new Service(ExtensionService.class.getName()));
        assertThat(module.getExtensions()).isEmpty();
    }

}
