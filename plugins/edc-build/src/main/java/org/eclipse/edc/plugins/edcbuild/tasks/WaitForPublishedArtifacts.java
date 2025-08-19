/*
 *  Copyright (c) 2025 Think-it GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Think-it GmbH - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.edcbuild.tasks;

import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.TaskAction;

import java.net.URL;
import java.time.LocalDateTime;

import static java.lang.Thread.sleep;
import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Task for root project that looks up for published artifacts, waiting if they are not available.
 * After a certain timeout the task fails
 */
public class WaitForPublishedArtifacts extends DefaultTask {

    private static final int TASK_TIMEOUT_MINUTES = 60;
    private static final int RETRY_WAIT_SECONDS = 30;

    @TaskAction
    public void waitForPublishedArtifacts() {
        if (!requireExtension(getProject(), BuildExtension.class).shouldPublish()) {
            return;
        }

        requireExtension(getProject(), PublishingExtension.class)
                .getPublications().stream()
                .map(MavenPublication.class::cast)
                .forEach(this::lookUpForPublication);
    }

    private void lookUpForPublication(MavenPublication publication) {
        if (publication.getVersion().endsWith("-SNAPSHOT")) {
            throw new IllegalStateException("This task can only be executed on proper releases, not on snapshots");
        }
        var artifact = publication.getName() + " " + publication.getGroupId() + ":" + publication.getArtifactId() + ":" + publication.getVersion();
        var repo = RepositoryHandler.MAVEN_CENTRAL_URL;

        var timeout = LocalDateTime.now().plusMinutes(TASK_TIMEOUT_MINUTES);
        var found = false;
        while (!found && timeout.isAfter(LocalDateTime.now())) {
            try {
                var url = repo + publication.getGroupId().replace('.', '/') +
                        "/" + publication.getArtifactId() + "/" + publication.getVersion() +
                        "/" + publication.getArtifactId() + "-" + publication.getVersion() +
                        ".pom";
                new URL(url).openStream().close();
                found = true;
            } catch (Throwable e) {
                getLogger().warn("Artifact {} is NOT available on maven central, will try again in {} seconds", artifact, RETRY_WAIT_SECONDS);
                try {
                    sleep(RETRY_WAIT_SECONDS * 1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (!found) {
            throw new IllegalStateException("Artifact %s has not been found maven central after %s minutes".formatted(artifact, TASK_TIMEOUT_MINUTES));
        }
    }
}
