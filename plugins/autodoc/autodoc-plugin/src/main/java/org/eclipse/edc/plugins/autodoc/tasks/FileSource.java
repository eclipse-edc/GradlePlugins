/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.plugins.autodoc.tasks;

import org.gradle.api.artifacts.Dependency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A dependency that is represented in the local file system, e.g. the local Maven cache
 */
public class FileSource extends DependencySource {
    /**
     * Instantiates a new file source
     *
     * @param dependency the dependency in question
     * @param uri        the location where the physical file exists
     * @param classifier what type of dependency we have, e.g. sources, sources, manifest etc
     * @param type       file extension
     */
    public FileSource(Dependency dependency, URI uri, String classifier, String type) {
        super(dependency, uri, classifier, type);
    }

    @Override
    public boolean exists() {
        return Files.exists(Path.of(uri()));
    }

    @Override
    public InputStream inputStream() {
        try {
            return new FileInputStream(new File(uri()));
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
