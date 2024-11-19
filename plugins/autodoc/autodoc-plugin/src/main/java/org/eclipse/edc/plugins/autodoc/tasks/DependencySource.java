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

import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

import static java.lang.String.format;

/**
 * Represents the combination of a dependency and a pointer (URL) to its physical location.
 */
public abstract class DependencySource {
    private final Dependency dependency;
    private final URI uri;
    private final String classifier;
    private final String type;

    /**
     * @param dependency the dependency in question
     * @param uri        the location where the physical file exists
     * @param classifier what type of dependency we have, e.g. sources, sources, manifest etc
     * @param type       file extension
     */
    public DependencySource(Dependency dependency, URI uri, String classifier, String type) {
        this.dependency = dependency;
        this.uri = uri;
        this.classifier = classifier;
        this.type = type;
    }

    /**
     * constructs the filename NAME-VERSION-CLASSIFIER.TYPE
     */
    String filename() {
        return format("%s-%s-%s.%s", dependency.getName(), dependency.getVersion(), classifier, type);
    }

    /**
     * Checks whether a dependency exists. In some implementations this may involve remote calls, so use this with prejudice!
     *
     * @return whether the dependency exists, i.e. the {@link DependencySource#uri()} points to a valid file
     */
    public abstract boolean exists();

    public Dependency dependency() {
        return dependency;
    }

    public URI uri() {
        return uri;
    }

    public String classifier() {
        return classifier;
    }

    public String type() {
        return type;
    }

    /**
     * Opens an input stream to the file located at {@link DependencySource#uri()}. It is highly recommended to check {@link DependencySource#exists()}
     * beforehand.
     *
     * @return Either the input stream to the file, or {@code null} if failed.
     */
    public abstract InputStream inputStream();
}
