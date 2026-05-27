/*
 *  Copyright (c) 2026 Think-it GmbH
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

package org.eclipse.edc.plugins.autodoc.core.processor.testconfig;

import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.SettingContext;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;
import org.eclipse.edc.spi.system.ServiceExtension;

import java.util.Map;

public class ServiceExtensionWithConfig implements ServiceExtension {

    @Configuration(context = "edc.configuration.context")
    private ConfigurationObject configuration;

    @Configuration(context = "edc.configuration.map")
    private Map<String, ConfigurationObject> configurationObjectMap;

    @SettingContext("edc.configuration.context.deprecated")
    @Configuration
    private ConfigurationObject configurationWithDeprecatedSettingContext;

    @Settings
    public record ConfigurationObject(
            @Setting(key = "setting") String setting
    ) {

    }
}
