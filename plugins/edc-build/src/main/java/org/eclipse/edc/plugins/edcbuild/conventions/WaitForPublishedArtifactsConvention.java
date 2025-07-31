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

package org.eclipse.edc.plugins.edcbuild.conventions;

import org.eclipse.edc.plugins.edcbuild.tasks.WaitForPublishedArtifacts;
import org.gradle.api.Project;

public class WaitForPublishedArtifactsConvention implements EdcConvention {
    @Override
    public void apply(Project target) {
        target.getTasks().register("waitForPublishedArtifacts", WaitForPublishedArtifacts.class);
    }
}
