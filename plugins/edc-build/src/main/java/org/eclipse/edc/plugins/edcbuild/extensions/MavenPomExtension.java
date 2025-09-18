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

package org.eclipse.edc.plugins.edcbuild.extensions;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class MavenPomExtension {

    private static final String PROJECT_URL = "https://projects.eclipse.org/projects/technology.edc";
    private String groupId = "org.eclipse.edc";

    @Inject
    public MavenPomExtension(Project project) {
        getProjectName().convention(project.getName());
        getDescription().convention("edc :: " + project.getName());
        getProjectUrl().convention(PROJECT_URL);
        getLicenseName().convention("The Apache License, Version 2.0");
        getLicenseUrl().convention("http://www.apache.org/licenses/LICENSE-2.0.txt");
        getDeveloperId().convention("mspiekermann");
        getDeveloperName().convention("Markus Spiekermann");
        getDeveloperEmail().convention("markus.spiekermann@isst.fraunhofer.de");
    }

    public abstract Property<String> getProjectName();

    public abstract Property<String> getDescription();

    public abstract Property<String> getProjectUrl();

    public abstract Property<String> getLicenseName();

    public abstract Property<String> getLicenseUrl();

    public abstract Property<String> getDeveloperId();

    public abstract Property<String> getDeveloperName();

    public abstract Property<String> getDeveloperEmail();

    public abstract Property<String> getScmConnection();

    public abstract Property<String> getScmUrl();

    @Deprecated(since = "edc-build-0.1.0")
    public String getGroupId() {
        return groupId;
    }

    @Deprecated(since = "edc-build-0.1.0")
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
