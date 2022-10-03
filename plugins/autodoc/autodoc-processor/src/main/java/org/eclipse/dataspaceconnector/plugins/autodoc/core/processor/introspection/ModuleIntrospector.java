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

import org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.AnnotationFunctions;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.EdcSetting;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.EdcSettingContext;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Extension;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.ExtensionPoint;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Inject;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provider;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provides;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Requires;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Spi;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.ConfigurationSetting;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.ModuleType;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.Service;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.ServiceReference;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import static java.util.stream.Collectors.toList;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.AnnotationFunctions.attributeStringValues;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.AnnotationFunctions.attributeTypeValues;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.AnnotationFunctions.attributeValue;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.AnnotationFunctions.mirrorFor;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.ElementFunctions.typeFor;

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
    public String getModuleName(ModuleType moduleType, RoundEnvironment environment) {
        if (ModuleType.EXTENSION == moduleType) {
            var extensionElement = getExtensionElements(environment).iterator().next();
            var annotationMirror = mirrorFor(Extension.class, extensionElement);
            return annotationMirror != null ?
                    attributeValue(String.class, "value", annotationMirror, elementUtils) :
                    extensionElement.getSimpleName().toString();
        } else {
            var extensionElement = environment.getElementsAnnotatedWith(Spi.class).iterator().next();
            return attributeValue(String.class, "value", mirrorFor(Spi.class, extensionElement), elementUtils);
        }
    }

    /**
     * Returns module categories set using either the {@link Spi} or {@link Extension} annotation.
     */
    public List<String> getModuleCategories(ModuleType moduleType, RoundEnvironment environment) {
        if (ModuleType.EXTENSION == moduleType) {
            var extensionElement = getExtensionElements(environment).iterator().next();
            var annotationMirror = mirrorFor(Extension.class, extensionElement);
            return annotationMirror != null ? attributeStringValues("categories", annotationMirror, elementUtils) : Collections.emptyList();
        } else {
            var extensionElement = environment.getElementsAnnotatedWith(Spi.class).iterator().next();
            return attributeStringValues("categories", mirrorFor(Spi.class, extensionElement), elementUtils);
        }
    }

    /**
     * Resolves referenced services by introspecting usages of {@link Inject}.
     */
    public List<ServiceReference> resolveReferencedServices(RoundEnvironment environment) {
        return environment.getElementsAnnotatedWith(Inject.class).stream()
                .map(element -> {
                    var required = attributeValue(Boolean.class, "required", mirrorFor(Inject.class, element), elementUtils);
                    return new ServiceReference(typeFor(element), required);
                })
                .collect(toList());
    }

    /**
     * Resolves referenced services by introspecting the {@link Provides} annotation.
     */
    public List<Service> resolveProvidedServices(RoundEnvironment environment) {
        var providesServices = environment.getElementsAnnotatedWith(Provides.class).stream()
                .flatMap(element -> attributeTypeValues("value", mirrorFor(Provides.class, element), elementUtils).stream());

        var providerMethodServices = environment.getElementsAnnotatedWith(Provider.class)
                .stream()
                .map(AnnotationFunctions::mirrorForReturn)
                .filter(Objects::nonNull)
                .map(TypeMirror::toString);

        return Stream.concat(providesServices, providerMethodServices)
                .distinct()
                .map(Service::new)
                .collect(toList());
    }

    /**
     * Resolves extension points declared with {@link ExtensionPoint}.
     */
    public List<Service> resolveExtensionPoints(RoundEnvironment environment) {
        return environment.getElementsAnnotatedWith(ExtensionPoint.class).stream()
                .map(element -> new Service(element.asType().toString()))
                .collect(toList());
    }

    /**
     * Resolves configuration points declared with {@link EdcSetting}.
     */
    public List<ConfigurationSetting> resolveConfigurationSettings(RoundEnvironment environment) {
        return environment.getElementsAnnotatedWith(EdcSetting.class).stream()
                .filter(VariableElement.class::isInstance)
                .map(VariableElement.class::cast)
                .map(this::createConfigurationSetting)
                .collect(toList());
    }

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
    public Set<Element> getExtensionElements(RoundEnvironment environment) {
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

    /**
     * Maps a {@link ConfigurationSetting} from an {@link EdcSetting} annotation.
     */
    private ConfigurationSetting createConfigurationSetting(VariableElement settingElement) {
        var prefix = resolveConfigurationPrefix(settingElement);
        var keyValue = prefix + settingElement.getConstantValue().toString();

        var settingBuilder = ConfigurationSetting.Builder.newInstance().key(keyValue);

        var settingMirror = mirrorFor(EdcSetting.class, settingElement);

        var description = attributeValue(String.class, "value", settingMirror, elementUtils);
        settingBuilder.description(description);

        var type = attributeValue(String.class, "type", settingMirror, elementUtils);
        settingBuilder.type(type);

        var required = attributeValue(Boolean.class, "required", settingMirror, elementUtils);
        settingBuilder.required(required);

        var max = attributeValue(Long.class, "max", settingMirror, elementUtils);
        settingBuilder.maximum(max);

        var min = attributeValue(Long.class, "min", settingMirror, elementUtils);
        settingBuilder.minimum(min);

        return settingBuilder.build();
    }

    /**
     * Resolves a configuration prefix specified by {@link EdcSettingContext} for a given EDC setting element or an empty string if there is none.
     */
    @NotNull
    private String resolveConfigurationPrefix(VariableElement edcSettingElement) {
        var enclosingElement = edcSettingElement.getEnclosingElement();
        if (enclosingElement == null) {
            return "";
        }
        var contextMirror = mirrorFor(EdcSettingContext.class, enclosingElement);
        return contextMirror != null ? attributeValue(String.class, "value", contextMirror, elementUtils) : "";
    }


}
