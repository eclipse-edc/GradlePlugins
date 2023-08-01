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

import org.eclipse.edc.plugins.autodoc.AutodocExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.tasks.TaskAction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class ManifestDownloadTask extends DefaultTask {

    private static final String EDC_GROUP = "org.eclipse.edc";
    private final HttpClient httpClient;

    public ManifestDownloadTask() {
        httpClient = HttpClient.newHttpClient();
    }

    @TaskAction
    public void downloadManifests() {
        var autodocExt = getProject().getExtensions().findByType(AutodocExtension.class);
        requireNonNull(autodocExt, "AutodocExtension cannot be null");

        getProject().getConfigurations()
                .stream().flatMap(config -> config.getDependencies().stream())
                .filter(dep -> EDC_GROUP.equals(dep.getGroup()))
                .filter(dep -> !getExclusions().contains(dep.getName()))
                .map(dep -> findInRepo(dep, "manifest", "json"))
                .filter(Optional::isPresent)
                .forEach(dt -> downloadDependency(dt.get(), getProject().getRootProject().getBuildDir().toPath().resolve("manifests")));
    }

    private String createArtifactUrl(Dependency dep, String classifier, String type, MavenArtifactRepository repo) {
        return format("%s%s/%s/%s/%s-%s-%s.%s", repo.getUrl(), dep.getGroup().replace(".", "/"), dep.getName(), dep.getVersion(),
                dep.getName(), dep.getVersion(), classifier, type);
    }

    private void downloadDependency(DownloadRequest dt, Path outputDirectory) {

        var p = outputDirectory.resolve(dt.filename());
        var request = HttpRequest.newBuilder().uri(dt.uri()).GET().build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                getLogger().warn("Could not download {}, HTTP response: {}", dt.dependency, response);
                return;
            }
            outputDirectory.toFile().mkdirs();
            getLogger().debug("Downloading {} into {}", dt, outputDirectory);
            try (var is = response.body(); var fos = new FileOutputStream(p.toFile())) {
                is.transferTo(fos);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<DownloadRequest> findInRepo(Dependency dep, String classifier, String type) {
        var repos = getProject().getRepositories().stream().toList();
        return repos.stream()
                .filter(repo -> repo instanceof MavenArtifactRepository)
                .map(repo -> (MavenArtifactRepository) repo)
                .map(repo -> {
                    var repoUrl = createArtifactUrl(dep, classifier, type, repo);
                    try {
                        // we use a HEAD request, because we only want to see whether that module has a `-manifest.json`
                        var uri = URI.create(repoUrl);
                        var headRequest = HttpRequest.newBuilder()
                                .uri(uri)
                                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                .build();
                        var response = httpClient.send(headRequest, HttpResponse.BodyHandlers.discarding());
                        if (response.statusCode() == 200) {
                            return new DownloadRequest(dep, uri, classifier, type);
                        }
                        return null;
                    } catch (IOException | InterruptedException | IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

    private Set<String> getExclusions() {
        return Set.of();
    }

    private record DownloadRequest(Dependency dependency, URI uri, String classifier, String type) {
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
}
