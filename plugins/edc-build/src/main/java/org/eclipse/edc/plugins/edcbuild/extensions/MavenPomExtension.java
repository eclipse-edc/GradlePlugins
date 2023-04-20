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

import org.gradle.api.provider.Property;

public abstract class MavenPomExtension {
    private final String groupId = "org.eclipse.edc";

    @Deprecated(since = "milestone9")
    public abstract Property<String> getProjectName();

    @Deprecated(since = "milestone9")
    public abstract Property<String> getDescription();

    @Deprecated(since = "milestone9")
    public abstract Property<String> getProjectUrl();

    public abstract Property<String> getLicenseName();

    public abstract Property<String> getLicenseUrl();

    public abstract Property<String> getDeveloperId();

    public abstract Property<String> getDeveloperName();

    public abstract Property<String> getDeveloperEmail();

    @Deprecated(since = "milestone9")
    public abstract Property<String> getScmConnection();

    @Deprecated(since = "milestone9")
    public abstract Property<String> getScmUrl();

    /**
     * Represent the org/repo string.
     * E.g. it should be "eclipse-edc/Connector" for the connector project
     *
     * @return a property representing the GitHub org/repo
     */
    public abstract Property<String> getGithubOrgRepo();

    public String getGroupId() {
        return groupId;
    }

}
