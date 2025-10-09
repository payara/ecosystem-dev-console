/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.rest.dto;

import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.inject.Qualifier;
import jakarta.decorator.Decorator;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple DTO to hold metadata about a CDI Decorator.
 */
public class DecoratorInfo {

    private final String className;
    private final Set<String> decoratedTypes;
    private final String delegateType;
    private final Set<String> delegateQualifiers;
    private final Set<String> classQualifiers;
    private final String scope;

    public DecoratorInfo(String className,
                         Set<String> decoratedTypes,
                         String delegateType,
                         Set<String> delegateQualifiers,
                         Set<String> classQualifiers,
                         String scope) {
        this.className = className;
        this.decoratedTypes = decoratedTypes;
        this.delegateType = delegateType;
        this.delegateQualifiers = delegateQualifiers;
        this.classQualifiers = classQualifiers;
        this.scope = scope;
    }

    public String getClassName() {
        return className;
    }

    public Set<String> getDecoratedTypes() {
        return decoratedTypes;
    }

    public String getDelegateType() {
        return delegateType;
    }

    public Set<String> getDelegateQualifiers() {
        return delegateQualifiers;
    }

    public Set<String> getClassQualifiers() {
        return classQualifiers;
    }

    public String getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return "DecoratorInfo{" +
                "className='" + className + '\'' +
                ", decoratedTypes=" + decoratedTypes +
                ", delegateType='" + delegateType + '\'' +
                ", delegateQualifiers=" + delegateQualifiers +
                ", classQualifiers=" + classQualifiers +
                ", scope='" + scope + '\'' +
                '}';
    }

    /**
     * Factory method: extract metadata from an AnnotatedType
     */
    public static DecoratorInfo fromAnnotatedType(AnnotatedType<?> at) {
        if (!at.isAnnotationPresent(Decorator.class)) {
            throw new IllegalArgumentException("Not a @Decorator type: " + at.getJavaClass());
        }

        // Class name
        String className = at.getJavaClass().getName();

        // Interfaces (decorated types)
        Set<String> decoratedTypes = Set.of(at.getJavaClass().getInterfaces())
                .stream()
                .map(Class::getName)
                .collect(Collectors.toSet());

        // Delegate injection point
        AnnotatedField<?> delegateField = at.getFields()
                .stream()
                .filter(f -> f.isAnnotationPresent(jakarta.decorator.Delegate.class))
                .findFirst()
                .orElse(null);

        String delegateType = delegateField != null
                ? delegateField.getBaseType().getTypeName()
                : null;

        // Delegate qualifiers
        Set<String> delegateQualifiers = delegateField != null
                ? delegateField.getAnnotations()
                    .stream()
                    .filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class))
                    .map(a -> a.annotationType().getName())
                    .collect(Collectors.toSet())
                : Set.of();

        // Class-level qualifiers
        Set<String> classQualifiers = at.getAnnotations()
                .stream()
                .filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class))
                .map(a -> a.annotationType().getName())
                .collect(Collectors.toSet());

        // Scope
        String scope = at.getAnnotations()
                .stream()
                .filter(a -> a.annotationType().isAnnotationPresent(jakarta.enterprise.context.NormalScope.class) ||
                             a.annotationType().isAnnotationPresent(jakarta.inject.Scope.class))
                .map(a -> a.annotationType().getName())
                .findFirst()
                .orElse("jakarta.enterprise.context.Dependent"); // default for decorators

        return new DecoratorInfo(className, decoratedTypes, delegateType, delegateQualifiers, classQualifiers, scope);
    }
}
