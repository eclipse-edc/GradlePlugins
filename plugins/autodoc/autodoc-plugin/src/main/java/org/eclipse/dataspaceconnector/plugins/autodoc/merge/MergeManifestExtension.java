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

package org.eclipse.dataspaceconnector.plugins.autodoc.merge;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.OutputFile;

import java.io.File;

public abstract class MergeManifestExtension {
    @OutputFile
    public abstract Property<File> getDestinationFile();
}
