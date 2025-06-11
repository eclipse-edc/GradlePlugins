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

public class Repositories {

    public static final String NEXUS_REPO_URL = "https://ossrh-staging-api.central.sonatype.com/service/local/";
    public static final String SNAPSHOT_REPO_URL = "https://central.sonatype.com/repository/maven-snapshots/";

    @Deprecated(since = "0.14.0")
    public static final String DEPRECATED_SNAPSHOT_REPO_URL = "https://oss.sonatype.org/content/repositories/snapshots/";

}
