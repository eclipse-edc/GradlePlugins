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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class SwaggerGeneratorExtension {

    private Set<String> resourcePackages = Set.of("org.eclipse.edc");
    private Set<ApiGroup> apiGroups = new HashSet<>();

    public abstract Property<String> getOutputFilename();

    public abstract Property<File> getOutputDirectory();

    public Set<String> getResourcePackages() {
        return resourcePackages;
    }

    public void setResourcePackages(Set<String> resourcePackages) {
        this.resourcePackages = resourcePackages;
    }

    /**
     * Get Api Group
     *
     * @return the property
     * @deprecated please use `apiGroup(name, packages...)` instead
     */
    @Deprecated(since = "1.5.0")
    public abstract Property<String> getApiGroup();

    public void apiGroup(String group, String... packages) {
        apiGroups.add(new ApiGroup(group, new HashSet<>(Arrays.asList(packages))));
    }

    public Set<ApiGroup> getApiGroups() {
        return apiGroups;
    }

}
