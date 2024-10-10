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

import java.net.URI;

import static java.lang.String.format;

/**
 * Represents the combination of a dependency and a pointer (URL) to its physical location.
 *
 * @param dependency the dependency in question
 * @param uri        the location where the physical file exists
 * @param classifier what type of dependency we have, e.g. sources, sources, manifest etc
 * @param type       file extension
 */
public record DependencySource(Dependency dependency, URI uri, String classifier, String type) {
    @Override
    public String toString() {
        return "{" +
                "dependency=" + dependency +
                ", uri=" + uri +
                '}';
    }

    String filename() {
        return format("%s-%s-%s.%s", dependency.getName(), dependency.getVersion(), classifier, type);
    }
}
