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
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Inject;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provider;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provides;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Spi;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.ConfigurationSetting;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.Service;
import org.eclipse.dataspaceconnector.runtime.metamodel.domain.ServiceReference;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.AnnotationFunctions.attributeStringValues;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.AnnotationFunctions.attributeTypeValues;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.AnnotationFunctions.attributeValue;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.AnnotationFunctions.mirrorFor;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.compiler.ElementFunctions.typeFor;
import static org.eclipse.dataspaceconnector.plugins.autodoc.core.processor.introspection.IntrospectionUtils.getExtensionElements;

public class ExtensionIntrospector {
    private final Elements elementUtils;

    public ExtensionIntrospector(Elements elementUtils) {
        this.elementUtils = elementUtils;
    }

    @NotNull
    private static <A extends Annotation> Stream<? extends Element> getEnclosedFieldsAnnotatedWith(Element extensionElement, Class<A> annotationClass) {
        return extensionElement.getEnclosedElements()
                .stream().filter(e -> e.getAnnotation(annotationClass) != null);
    }

    /**
     * Returns module categories set using either the {@link Spi} or {@link Extension} annotation.
     */
    public List<String> getExtensionCategories(RoundEnvironment environment) {
        var extensionElement = getExtensionElements(environment).iterator().next();
        var annotationMirror = mirrorFor(Extension.class, extensionElement);
        return annotationMirror != null ? attributeStringValues("categories", annotationMirror, elementUtils) : Collections.emptyList();
    }

    /**
     * Resolves referenced services by introspecting usages of {@link Inject}.
     */
    public List<ServiceReference> resolveReferencedServices(Element extensionElement) {
        return getEnclosedFieldsAnnotatedWith(extensionElement, Inject.class)
                .map(element -> {
                    var required = attributeValue(Boolean.class, "required", mirrorFor(Inject.class, element), elementUtils);
                    return new ServiceReference(typeFor(element), required);
                })
                .collect(toList());
    }

    /**
     * Resolves referenced services by introspecting the {@link Provides} annotation.
     */
    public List<Service> resolveProvidedServices(RoundEnvironment environment, Element element) {

        var providesServices = ofNullable(mirrorFor(Provides.class, element))
                .map(mirror -> attributeTypeValues("value", mirror, elementUtils).stream())
                .orElse(Stream.empty());

        var providerMethodServices = getEnclosedFieldsAnnotatedWith(element, Provider.class)
                .map(AnnotationFunctions::mirrorForReturn)
                .filter(Objects::nonNull)
                .map(TypeMirror::toString);

        return Stream.concat(providesServices, providerMethodServices)
                .distinct()
                .map(Service::new)
                .collect(toList());
    }

    /**
     * Resolves configuration points declared with {@link EdcSetting}.
     */
    public List<ConfigurationSetting> resolveConfigurationSettings(Element element) {
        return getEnclosedFieldsAnnotatedWith(element, EdcSetting.class)
                .filter(VariableElement.class::isInstance)
                .map(VariableElement.class::cast)
                .map(this::createConfigurationSetting)
                .collect(toList());
    }

    public String getExtensionName(Element extensionElement) {
        var annotationMirror = mirrorFor(Extension.class, extensionElement);
        return annotationMirror != null ?
                attributeValue(String.class, "value", annotationMirror, elementUtils) :
                extensionElement.getSimpleName().toString();
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
