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

class DependencySourceFactory {
    public static DependencySource createDependencySource(URI uri, Dependency dependency, String classifier, String type) {
        if (uri.getScheme().equals("file")) {
            return new FileSource(dependency, uri, classifier, type);
        } else if (uri.getScheme().startsWith("http")) {
            return new HttpSource(dependency, uri, classifier, type);
        } else {
            throw new RuntimeException("Unknown URI scheme " + uri);
        }
    }
}
