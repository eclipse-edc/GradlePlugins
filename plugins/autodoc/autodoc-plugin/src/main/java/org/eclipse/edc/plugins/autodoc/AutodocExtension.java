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

package org.eclipse.edc.plugins.autodoc;

import org.gradle.api.provider.Property;

import java.io.File;

public abstract class AutodocExtension {
    /**
     * Overrides the default output directory relative to the current project dir
     */
    public abstract Property<File> getOutputDirectory();

    /**
     * Override the version of the annotation processor module to use. The default is to take the same version as the plugin.
     */
    public abstract Property<String> getProcessorVersion();

    /**
     * Optional input to specify, where additional autodoc manifests that are to be merged, are located on the filesystem.
     * Use this, if you have a directory that contains multiple autodoc manifests, e.g. of third-party or transitive deps.
     * <p>
     * If this is set, the merge task will take all manifests found in this directory and append them to the {@code manifest.json} file.
     */
    public abstract Property<File> getAdditionalInputDirectory();

}
