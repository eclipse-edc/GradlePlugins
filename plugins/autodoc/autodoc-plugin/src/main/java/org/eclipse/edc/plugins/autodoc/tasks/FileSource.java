package org.eclipse.edc.plugins.autodoc.tasks;

import org.gradle.api.artifacts.Dependency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

/**
 * A dependency that is represented in the local file system, e.g. the local Maven cache
 */
public class FileSource extends DependencySource {
    /**
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
        return new File(uri()).exists();
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
