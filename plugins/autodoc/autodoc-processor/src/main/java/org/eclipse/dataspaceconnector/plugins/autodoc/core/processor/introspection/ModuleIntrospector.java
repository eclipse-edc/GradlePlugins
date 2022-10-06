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

package org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.introspection;

import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Extension;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.ExtensionPoint;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Spi;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.Service;

import java.util.List;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Elements;

import static java.util.stream.Collectors.toList;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.AnnotationFunctions.attributeValue;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.AnnotationFunctions.mirrorFor;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.introspection.IntrospectionUtils.getExtensionElements;

/**
 * Contains methods for introspecting the current module using the Java Compiler API.
 */
public class ModuleIntrospector {
    private final Elements elementUtils;

    public ModuleIntrospector(Elements elementUtils) {
        this.elementUtils = elementUtils;
    }

    /**
     * Returns the module name set by either the {@link Spi} or {@link Extension} annotation.
     */
    public List<String> getExtensionNames(RoundEnvironment environment) {
        return getExtensionElements(environment).stream().map(extensionElement -> {
            var annotationMirror = mirrorFor(Extension.class, extensionElement);
            return annotationMirror != null ?
                    attributeValue(String.class, "value", annotationMirror, elementUtils) :
                    extensionElement.getSimpleName().toString();
        }).collect(toList());
    }

    /**
     * Resolves extension points declared with {@link ExtensionPoint}.
     */
    public List<Service> resolveExtensionPoints(RoundEnvironment environment) {
        return environment.getElementsAnnotatedWith(ExtensionPoint.class).stream()
                .map(element -> new Service(element.asType().toString()))
                .collect(toList());
    }

    public String getModuleName(RoundEnvironment environment) {
        var extensionElement = environment.getElementsAnnotatedWith(Spi.class).iterator().next();
        return attributeValue(String.class, "value", mirrorFor(Spi.class, extensionElement), elementUtils);
    }


}
