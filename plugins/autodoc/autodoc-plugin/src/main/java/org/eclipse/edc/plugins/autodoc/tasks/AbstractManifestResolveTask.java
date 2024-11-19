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

import org.eclipse.edc.plugins.autodoc.AutodocExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.eclipse.edc.plugins.autodoc.tasks.Constants.DEFAULT_AUTODOC_FOLDER;

/**
 * Abstract gradle task, that "resolves" an already-existing autodoc manifest from a URI and transfers (=copies, downloads,...)
 * the file to a directory on the local file system.
 * <p>
 * Implementations must provide a reference to that autodoc manifest file in the form of a {@link DependencySource}.
 */
public abstract class AbstractManifestResolveTask extends DefaultTask {
    public static final String MANIFEST_CLASSIFIER = "manifest";
    public static final String MANIFEST_TYPE = "json";
    protected Path downloadDirectory;
    private File outputDirectoryOverride;

    public AbstractManifestResolveTask() {
        downloadDirectory = getProject().getLayout().getBuildDirectory().getAsFile().get().toPath().resolve(DEFAULT_AUTODOC_FOLDER);
    }

    @TaskAction
    public void resolveAutodocManifest() {
        var autodocExt = getProject().getExtensions().findByType(AutodocExtension.class);
        requireNonNull(autodocExt, "AutodocExtension cannot be null");

        if (autodocExt.getDownloadDirectory().isPresent()) {
            downloadDirectory = autodocExt.getDownloadDirectory().get().toPath();
        }

        if (outputDirectoryOverride != null) {
            downloadDirectory = outputDirectoryOverride.toPath();
        }

        getProject().getConfigurations()
                .stream().flatMap(config -> config.getDependencies().stream())
                .distinct()
                .filter(this::dependencyFilter)
                .filter(dep -> !getExclusions().contains(dep.getName()))
                .map(this::createSource)
                .filter(Optional::isPresent)
                .forEach(dt -> transferDependencyFile(dt.get(), downloadDirectory));
    }

    @Option(option = "output", description = "CLI option to override the output directory")
    public void setOutput(String output) {
        this.outputDirectoryOverride = new File(output);
    }

    protected abstract boolean dependencyFilter(Dependency dependency);

    /**
     * Returns an {@link InputStream} that points to the physical location of the autodoc manifest file.
     */
    @Internal //otherwise it would get interpreted as task input :/
    protected abstract InputStream resolveManifest(DependencySource autodocManifest);

    @Internal //otherwise it would get interpreted as task input :/
    protected Set<String> getExclusions() {
        return Set.of();
    }

    @Internal //otherwise it would get interpreted as task input :/
    protected abstract Optional<DependencySource> createSource(Dependency dependency);

    private void transferDependencyFile(DependencySource dependencySource, Path downloadDirectory) {
        var targetFilePath = downloadDirectory.resolve(dependencySource.filename());
        try (var inputStream = resolveManifest(dependencySource)) {
            if (inputStream != null) {
                downloadDirectory.toFile().mkdirs();
                getLogger().debug("Downloading {} into {}", dependencySource, downloadDirectory);
                try (var fos = new FileOutputStream(targetFilePath.toFile())) {
                    inputStream.transferTo(fos);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
