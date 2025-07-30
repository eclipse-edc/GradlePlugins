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

import com.vanniktech.maven.publish.MavenPublishBaseExtension;
import org.gradle.api.Project;

class PublishToMavenCentralConvention implements EdcConvention {

    @Override
    public void apply(Project target) {
        target.getExtensions().configure(MavenPublishBaseExtension.class, extension -> {
            extension.publishToMavenCentral(true);
        });
    }

}
