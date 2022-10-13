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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;

class JsonFileAppender {
    private static final ReentrantLock LOCK = new ReentrantLock();
    private final Logger logger;
    private final ObjectMapper mapper;

    JsonFileAppender(Logger logger) {
        this.logger = logger;
        mapper = new ObjectMapper();
    }

    public void append(File destination, File source) {
        logger.lifecycle(format("Appending contents of [%s] to [%s]", source, destination));

        checkOrCreate(destination);
        if (!source.exists()) {
            throw new GradleException(format("Source file [%s] does not exist!", source));
        }

        LOCK.lock();
        try {
            var srcContent = readJsonFile(source);
            var targetContent = readJsonFile(destination);

            var newContent = new ArrayList<>();
            newContent.addAll(targetContent);
            newContent.addAll(srcContent);

            writeJsonFile(newContent, destination);
        } catch (IOException e) {
            throw new GradleException("Error reading input manifest", e);
        } finally {
            LOCK.unlock();
        }

    }

    private void writeJsonFile(List<?> content, File destination) throws IOException {
        mapper.writeValue(destination, content);
    }

    private List<?> readJsonFile(File source) throws IOException {
        var tr = new TypeReference<List<?>>() {
        };
        try {
            return mapper.readValue(source, tr);
        } catch (IOException ex) {
            return Collections.emptyList();
        }
    }

    private void checkOrCreate(File destination) {
        if (!destination.exists()) {
            logger.warn("Destination file {} does not exist, creating...", destination);
            try {
                if (!destination.createNewFile()) {
                    destinationError(destination, null);
                }
            } catch (IOException e) {
                destinationError(destination, e);
            }
        }
    }

    private void destinationError(File destination, Throwable t) {
        logger.error("Could not create destination file {}", destination);
        throw new GradleException("Error creating destination file", t);
    }
}
