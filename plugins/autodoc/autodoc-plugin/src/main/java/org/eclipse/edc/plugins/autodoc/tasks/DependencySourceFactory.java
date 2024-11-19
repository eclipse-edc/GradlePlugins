package org.eclipse.edc.plugins.autodoc.tasks;

import org.gradle.api.artifacts.Dependency;

import java.net.URI;

class DependencySourceFactory {
    public static DependencySource createDependencySource(URI uri, Dependency dependency, String classifier, String type) {
        if (uri.getScheme().equals("file")) {
            return new FileSource(dependency, uri, classifier, type);
        } else if (uri.getScheme().startsWith("http")) {
            return new HttpSource(dependency, uri, classifier, type);
        }
        else throw new RuntimeException("Unknown URI scheme " + uri);
    }
}
