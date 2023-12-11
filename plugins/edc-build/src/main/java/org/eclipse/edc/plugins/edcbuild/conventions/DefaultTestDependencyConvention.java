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

package org.eclipse.edc.plugins.edcbuild.conventions;

import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.VersionConstraint;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import org.gradle.api.internal.catalog.VersionModel;

import java.util.Optional;

import static java.lang.String.format;
import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;
import static org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME;
import static org.gradle.api.plugins.JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME;

/**
 * Applies default test dependencies to all "java-library" projects: JUnit, Mockito and AssertJ in their respective
 * configurations.
 */
class DefaultTestDependencyConvention implements EdcConvention {

    private static final String DEFAULT_MOCKITO_VERSION = "5.8.0";
    private static final String DEFAULT_ASSERTJ_VERSION = "3.24.2";
    private static final String DEFAULT_JUPITER_VERSION = "5.10.1";

    @Override
    public void apply(Project target) {
        target.getPluginManager().withPlugin("java-library", plugin -> {

            var ext = requireExtension(target, BuildExtension.class).getVersions();
            var catalogReader = new CatalogReader(target, ext.getCatalogName());
            var d = target.getDependencies();

            //test classpath dependencies
            var jupiterVersion = ext.getJupiter().getOrElse(catalogReader.versionFor("jupiter", DEFAULT_JUPITER_VERSION));
            var mockitoVersion = ext.getMockito().getOrElse(catalogReader.versionFor("mockito", DEFAULT_MOCKITO_VERSION));
            var assertjVersion = ext.getAssertJ().getOrElse(catalogReader.versionFor("assertj", DEFAULT_ASSERTJ_VERSION));
            d.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-api:%s", jupiterVersion));
            d.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-params:%s", jupiterVersion));
            d.add(TEST_RUNTIME_ONLY_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-engine:%s", jupiterVersion));
            d.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.mockito:mockito-core:%s", mockitoVersion));
            d.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.assertj:assertj-core:%s", assertjVersion));
        });
    }

    private static class CatalogReader {
        private static final String FIELDNAME_CONFIG = "config";
        private final Project target;
        private final String catalogName;

        CatalogReader(Project target, String catalogName) {
            this.target = target;
            this.catalogName = catalogName;
        }

        String versionFor(String versionRef, String defaultValue) {
            var factory = target.getExtensions().findByName(catalogName);
            if (factory == null) {
                target.getLogger().debug("No VersionCatalog with name {} found. Please either override the version for {} in your build script, or instantiate the version catalog in your client project.", catalogName, versionRef);
                return defaultValue;
            }
            try {
                var field = AbstractExternalDependencyFactory.class.getDeclaredField(FIELDNAME_CONFIG);
                field.setAccessible(true);
                var catalog = (DefaultVersionCatalog) field.get(factory);
                return Optional.ofNullable(catalog)
                        .map(c -> c.getVersion(versionRef))
                        .map(VersionModel::getVersion)
                        .map(VersionConstraint::getRequiredVersion)
                        .orElse(defaultValue);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new GradleException("error introspecting catalog", e);
            }
        }
    }
}
