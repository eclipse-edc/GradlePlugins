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

package org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.testextensions;

import org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.Constants;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.EdcSetting;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Inject;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provider;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provides;

@Provides(SomeOtherService.class)
public class SampleExtensionWithoutAnnotation {
    @EdcSetting(value = Constants.TEST_SETTING_NAME, required = true)
    public static final String TEST_SETTING = Constants.TEST_SETTING_KEY;

    @Inject
    protected RequiredService requiredService;

    @Inject(required = false)
    protected OptionalService optionalService;

    @Provider
    public SomeService provideSomeService() {
        return null;
    }

}
