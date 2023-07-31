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

package org.eclipse.edc.plugins.autodoc.spi;

import org.eclipse.edc.runtime.metamodel.domain.ConfigurationSetting;
import org.eclipse.edc.runtime.metamodel.domain.EdcServiceExtension;
import org.eclipse.edc.runtime.metamodel.domain.ModuleType;
import org.eclipse.edc.runtime.metamodel.domain.Service;
import org.eclipse.edc.runtime.metamodel.domain.ServiceReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.List;

/**
 * The ManifestRenderer interface provides callback methods to render a manifest document.
 */
public interface ManifestRenderer {
    String DOCUMENT_HEADING = "EDC Autodoc Manifest";
    String EXTENSION_POINTS = "Extension points";
    String EXTENSIONS = "Extensions";
    String NONE = "None";

    /**
     * Renders the document header
     */
    void handleDocumentHeader();

    /**
     * Render the heading for a module.
     */
    void handleModuleHeading(@Nullable String moduleName, @NotNull String modulePath, @NotNull String version);

    /**
     * Render a document section for the categories of a module.
     *
     * @param categories May be empty, may contain empty strings.
     */
    void handleModuleCategories(List<String> categories);

    /**
     * Handles the creation of an {@link EdcServiceExtension} object, which usually represents all the services in a module, that are intended to be implemented or subclassed.
     */
    void handleExtensionPoints(List<Service> extensionPoints);

    /**
     * Create a sub-heading for an extension
     */
    void handleExtensionHeading();

    /**
     * Render the header for an extension.
     *
     * @param className The fully-qualified java classname.
     * @param name      The human-readable extension name. May be null.
     * @param overview  A string containing more information about the extension. Can be null, empty or even multiline.
     * @param type      The type of extension module, it can either be an SPI module or an implementation module
     */
    void handleExtensionHeader(@NotNull String className, @Nullable String name, @Nullable String overview, ModuleType type);

    /**
     * Render all configuration values that are declared by a particular extension
     */
    void handleConfigurations(List<ConfigurationSetting> configuration);

    /**
     * Render all services, that are <em>provided</em> by a particular module.
     */
    void handleExposedServices(List<Service> provides);

    /**
     * Render all services, that an extension <em>requires</em>, i.e. that must be provided by <em>other extensions</em>.
     */
    void handleReferencedServices(List<ServiceReference> references);

    /**
     * Finalizes the conversion, e.g. by adding closing tags, adding footnotes, validation, etc.
     *
     * @return An {@link OutputStream} that contains the rendered document.
     */
    OutputStream finalizeRendering();
}
