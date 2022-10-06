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

import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.EdcSetting;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Extension;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Inject;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provider;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provides;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Requires;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

public class IntrospectionUtils {

    /**
     * Get all {@link Element}s that fulfill any of the following criteria:
     * <ul>
     *     <li>Are annotated with {@link Extension}</li>
     *     <li>Are annotated with {@link Provides}</li>
     *     <li>Are annotated with {@link Requires}</li>
     *     <li>Have one or more fields annotated with {@link Inject}</li>
     *     <li>Have one or more fields annotated with {@link EdcSetting}</li>
     *     <li>Have one or more methods annotated with {@link Provider}</li>
     * </ul>
     * <p>
     * Note that elements are pruned, i.e. every extension only occurs once. This is important because extensions that have multiple
     * relevant fields and are annotated, will only occur once in the result.
     *
     * @param environment the {@link RoundEnvironment} that is passed in to the annotation processor
     * @return a set containing the distinct extension symbols. Elements in that set are most likely of type Symbol.ClassSymbol
     */
    public static Set<Element> getExtensionElements(RoundEnvironment environment) {
        var extensionClasses = environment.getElementsAnnotatedWith(Extension.class);
        var settingsSymbols = environment.getElementsAnnotatedWith(EdcSetting.class);
        var injectSymbols = environment.getElementsAnnotatedWith(Inject.class);
        var providerSymbols = environment.getElementsAnnotatedWith(Provider.class);
        var providesClasses = environment.getElementsAnnotatedWith(Provides.class);
        var requiresClasses = environment.getElementsAnnotatedWith(Requires.class);

        var symbols = settingsSymbols.stream();
        symbols = Stream.concat(symbols, injectSymbols.stream());
        symbols = Stream.concat(symbols, providerSymbols.stream());

        var classes = symbols.map(Element::getEnclosingElement).collect(Collectors.toSet());
        classes.addAll(requiresClasses);
        classes.addAll(providesClasses);
        classes.addAll(extensionClasses);

        return classes;
    }
}
