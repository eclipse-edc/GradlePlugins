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

package org.eclipse.edc.plugins.autodoc.tasks;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;

public class DownloadManifestTask extends AbstractManifestResolveTask {

    public static final String NAME = "downloadManifests";
    private static final Duration MAX_MANIFEST_AGE = Duration.ofHours(24);

    @Override
    protected boolean includeDependency(Dependency dependency) {
        return !(dependency instanceof ProjectDependency);
    }

    @Override
    protected InputStream resolveManifest(DependencySource autodocManifest) {
        var inputStream = autodocManifest.inputStream();
        if (inputStream == null) {
            getLogger().warn("Could not obtain {}", autodocManifest.dependency());
            return null;
        }
        return inputStream;
    }

    /**
     * Creates a download request for a given dependency, classifier, and type. A download request is successfully created if:
     * <ul>
     *     <li>the file does not exist locally, e.g. from a previous run</li>
     *     <li>the file exists locally, but is too old (older than 24hrs) </li>
     *     <li>the file exists locally, but is not readable</li>
     *     <li>the file is found in at least one Maven repository. MavenLocal is ignored.</li>
     * </ul>
     *
     * @param dependency the dependency to download
     * @return an optional DownloadRequest if the artifact can be downloaded, otherwise an empty optional
     */
    @Override
    protected Optional<DependencySource> createSource(Dependency dependency) {
        if (isLocalFileValid(dependency)) {
            getLogger().debug("Local file {} was deemed to be viable, will not download", dependency);
            return Optional.empty();
        }
        var repos = getProject().getRepositories().stream().toList();
        return repos.stream()
                .filter(repo -> repo instanceof MavenArtifactRepository)
                .map(repo -> (MavenArtifactRepository) repo)
                .map(repo -> {
                    var repoUrl = createArtifactUrl(dependency, repo);
                    try {
                        var ds = DependencySourceFactory.createDependencySource(URI.create(repoUrl), dependency, MANIFEST_CLASSIFIER, MANIFEST_TYPE);
                        if (ds.exists()) {
                            getLogger().debug("Manifest found for '{}' at {}", dependency.getName(), ds.uri());
                            return ds;
                        }
                        getLogger().debug("Manifest not found for '{}' at {}", dependency.getName(), ds.uri());
                        return null;
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

    private String createArtifactUrl(Dependency dep, MavenArtifactRepository repo) {
        return format("%s%s/%s/%s/%s-%s-%s.%s", repo.getUrl(), dep.getGroup().replace(".", "/"), dep.getName(), dep.getVersion(),
                dep.getName(), dep.getVersion(), MANIFEST_CLASSIFIER, MANIFEST_TYPE);
    }

    /**
     * Checks if the manifest for a dependency exists locally. A local file is considered valid if:
     * <ul>
     *  <li>The output directory exists</li>
     *  <li>The file exists locally and is readable</li>
     *  <li>The file is not older than 24 hours</li>
     * </ul>
     *
     * @param dep the dependency to check
     * @return true if the local file is valid, false otherwise
     */
    private boolean isLocalFileValid(Dependency dep) {
        if (!downloadDirectory.toFile().exists()) return false;
        var filename = format("%s-%s-%s.%s", dep.getName(), dep.getVersion(), MANIFEST_CLASSIFIER, MANIFEST_TYPE);
        var filePath = downloadDirectory.resolve(filename);
        var file = filePath.toFile();
        if (!file.exists() || !file.canRead()) return false;

        try {
            var date = Files.getLastModifiedTime(filePath).toInstant();
            return Duration.between(date, Instant.now()).compareTo(MAX_MANIFEST_AGE) <= 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
