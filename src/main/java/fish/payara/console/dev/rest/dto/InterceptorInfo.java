/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.rest.dto;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedType;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO holding metadata about a CDI Interceptor.
 */
public class InterceptorInfo {

    private final String className;
    private final Set<String> interceptorBindings;
    private final int priority;
    private final String scope;
    private final Set<String> classQualifiers;

    public InterceptorInfo(String className,
            Set<String> interceptorBindings,
            int priority,
            String scope,
            Set<String> classQualifiers) {
        this.className = className;
        this.interceptorBindings = interceptorBindings;
        this.priority = priority;
        this.scope = scope;
        this.classQualifiers = classQualifiers;
    }

    public String getClassName() {
        return className;
    }

    public Set<String> getInterceptorBindings() {
        return interceptorBindings;
    }

    public int getPriority() {
        return priority;
    }

    public String getScope() {
        return scope;
    }

    public Set<String> getClassQualifiers() {
        return classQualifiers;
    }

    @Override
    public String toString() {
        return "InterceptorInfo{"
                + "className='" + className + '\''
                + ", interceptorBindings=" + interceptorBindings
                + ", priority=" + priority
                + ", scope='" + scope + '\''
                + ", classQualifiers=" + classQualifiers
                + '}';
    }

    public static InterceptorInfo fromAnnotated(Annotated annotated) {
        if (!(annotated instanceof AnnotatedType<?> at)) {
            throw new IllegalArgumentException("Expected AnnotatedType for interceptor, got: " + annotated);
        }

        if (!at.isAnnotationPresent(jakarta.interceptor.Interceptor.class)) {
            throw new IllegalArgumentException("Not an @Interceptor type: " + at.getJavaClass());
        }

        String className = at.getJavaClass().getName();

        // Extract interceptor bindings
        Set<String> interceptorBindings = at.getAnnotations()
                .stream()
                .filter(a -> a.annotationType().isAnnotationPresent(jakarta.interceptor.InterceptorBinding.class))
                .map(a -> a.annotationType().getName())
                .collect(Collectors.toSet());

        // Extract @Priority if present
        int priority = at.isAnnotationPresent(jakarta.annotation.Priority.class)
                ? at.getAnnotation(jakarta.annotation.Priority.class).value()
                : 0;

        // Scope (default is @Dependent)
        String scope = at.getAnnotations()
                .stream()
                .filter(a -> a.annotationType().isAnnotationPresent(jakarta.enterprise.context.NormalScope.class)
                || a.annotationType().isAnnotationPresent(jakarta.inject.Scope.class))
                .map(a -> a.annotationType().getName())
                .findFirst()
                .orElse("jakarta.enterprise.context.Dependent");

        // Class-level qualifiers
        Set<String> classQualifiers = at.getAnnotations()
                .stream()
                .filter(a -> a.annotationType().isAnnotationPresent(jakarta.inject.Qualifier.class))
                .map(a -> a.annotationType().getName())
                .collect(Collectors.toSet());

        return new InterceptorInfo(className, interceptorBindings, priority, scope, classQualifiers);
    }

}
