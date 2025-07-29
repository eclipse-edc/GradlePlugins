/*
 *  Copyright (c) 2022 - 2023 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 */

package org.eclipse.edc.plugins.edcbuild.conventions;

import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.eclipse.edc.plugins.edcbuild.extensions.MavenPomExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;

import java.io.File;
import java.net.URL;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;
import static org.gradle.api.artifacts.ArtifactRepositoryContainer.MAVEN_CENTRAL_URL;

/**
 * Configures the Maven POM for each project:
 * <ul>
 *     <li>sets project name, description, license, SCM info etc.</li>
 *     <li>adds an artifact for the documentation manifest ("edc.json")</li>
 * </ul>
 */
class MavenArtifactConvention implements EdcConvention {

    private static final String PROJECT_URL = "https://projects.eclipse.org/projects/technology.edc";

    @Override
    public void apply(Project target) {
        target.afterEvaluate(project -> {
            var pubExt = requireExtension(project, PublishingExtension.class);
            var pomExt = requireExtension(project, BuildExtension.class).getPom();

            pubExt.getPublications().stream()
                    .filter(MavenPublication.class::isInstance)
                    .map(MavenPublication.class::cast)
                    .peek(mavenPub -> mavenPub.pom(pom -> setPomInformation(pomExt, target, pom)))
                    .peek(publication -> {
                        if (!publication.getVersion().endsWith("-SNAPSHOT")) {
                            disablePublishIfArtifactAlreadyUploaded(publication, project);
                        }
                    })
                    .forEach(mavenPub -> {
                        var openapiFiles = target.getLayout().getBuildDirectory().getAsFile().get().toPath()
                                .resolve("docs").resolve("openapi").toFile()
                                .listFiles((dir, name) -> name.endsWith(".yaml"));

                        if (openapiFiles != null) {
                            for (var openapiFile : openapiFiles) {
                                addArtifactIfExist(target, openapiFile, mavenPub, artifact -> {
                                    artifact.setClassifier(getFilenameWithoutExtension(openapiFile));
                                    artifact.setType("yaml");
                                    artifact.builtBy("openapi");
                                });
                            }
                        }

                    });
        });
    }

    private void disablePublishIfArtifactAlreadyUploaded(MavenPublication publication, Project project) {
        project.getTasks().withType(PublishToMavenRepository.class).forEach(task -> {
            var artifact = publication.getGroupId() + ":" + publication.getArtifactId() + ":" + publication.getVersion();

            try {
                var artifactPath = MAVEN_CENTRAL_URL + publication.getGroupId().replace('.', '/') +
                        "/" + publication.getArtifactId() + "/" + publication.getVersion() +
                        "/" + publication.getArtifactId() + "-" + publication.getVersion() + ".pom";

                new URL(artifactPath).openStream().close();
                project.getLogger().debug("Artifact {} already exists - skipping publish", artifact);
                task.setEnabled(false);
            } catch (Throwable e) {
                project.getLogger().debug("Artifact {} not found on maven central - will publish", artifact);
            }
        });
    }

    private String getFilenameWithoutExtension(File openapiFile) {
        return openapiFile.getName().substring(0, openapiFile.getName().lastIndexOf("."));
    }

    private void addArtifactIfExist(Project project, File location, MavenPublication mavenPublication, Action<ConfigurablePublishArtifact> configureAction) {
        if (location.exists()) {
            mavenPublication.getArtifacts()
                    .artifact(project.getArtifacts().add("archives", location, configureAction));
        }
    }

    private static void setPomInformation(MavenPomExtension pomExt, Project project, MavenPom pom) {
        // these properties are mandatory!
        var projectName = pomExt.getProjectName().getOrElse(project.getName());
        var description = pomExt.getDescription().getOrElse("edc :: " + project.getName());
        var projectUrl = pomExt.getProjectUrl().getOrElse(PROJECT_URL);
        pom.getName().set(projectName);
        pom.getDescription().set(description);
        pom.getUrl().set(projectUrl);

        // we'll provide a sane default for these properties
        pom.licenses(l -> l.license(pl -> {
            pl.getName().set(pomExt.getLicenseName().getOrElse("The Apache License, Version 2.0"));
            pl.getUrl().set(pomExt.getLicenseUrl().getOrElse("http://www.apache.org/licenses/LICENSE-2.0.txt"));
        }));

        pom.developers(d -> d.developer(md -> {
            md.getId().set(pomExt.getDeveloperId().getOrElse("mspiekermann"));
            md.getName().set(pomExt.getDeveloperName().getOrElse("Markus Spiekermann"));
            md.getEmail().set(pomExt.getDeveloperEmail().getOrElse("markus.spiekermann@isst.fraunhofer.de"));
        }));

        pom.scm(scm -> {
            scm.getUrl().set(pomExt.getScmUrl());
            scm.getConnection().set(pomExt.getScmConnection());
        });
    }

}
