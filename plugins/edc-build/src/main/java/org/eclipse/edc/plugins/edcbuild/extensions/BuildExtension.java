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
 *       Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V. - add publish property
 *
 */

package org.eclipse.edc.plugins.edcbuild.extensions;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

/**
 * Root configuration resource for the EDC Build plugin
 */
public abstract class BuildExtension {

    private static final boolean DEFAULT_SHOULD_PUBLISH = true;

    private final MavenPomExtension pom;
    private final SwaggerGeneratorExtension swagger;

    public BuildExtension(ObjectFactory objectFactory) {
        pom = objectFactory.newInstance(MavenPomExtension.class);
        swagger = objectFactory.newInstance(SwaggerGeneratorExtension.class);
    }

    public void pom(Action<? super MavenPomExtension> action) {
        action.execute(pom);
    }

    public void swagger(Action<? super SwaggerGeneratorExtension> action) {
        action.execute(swagger);
    }

    public MavenPomExtension getPom() {
        return pom;
    }

    public abstract Property<JavaLanguageVersion> getJavaLanguageVersion();

    public SwaggerGeneratorExtension getSwagger() {
        return swagger;
    }

    /**
     * Tell if the module needs to be published
     *
     * @return true if the module needs to be published, false otherwise
     */
    public boolean shouldPublish() {
        return getPublish().getOrElse(DEFAULT_SHOULD_PUBLISH);
    }

    public abstract Property<Boolean> getPublish();

}
