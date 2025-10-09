package fish.payara.console.dev.rest.dto;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InterceptedClassInfo {

    private final String className;
    private final Set<String> interceptorBindings;

    public InterceptedClassInfo(Bean<?> bean) {
        this.className = bean.getBeanClass().getName();

        this.interceptorBindings = findInterceptorBindings(bean.getBeanClass())
                .map(InterceptedClassInfo::formatAnnotation)
                .collect(Collectors.toSet());
    }

    private static Stream<Annotation> findInterceptorBindings(Class<?> beanClass) {
        // all annotations directly on the bean class
        return Stream.of(beanClass.getAnnotations())
                .filter(a -> a.annotationType().isAnnotationPresent(InterceptorBinding.class));
    }

    
    private static String formatAnnotation(Annotation a) {
    var type = a.annotationType();
    if (type.getDeclaredMethods().length == 0) {
        // marker binding
        return "@" + type.getSimpleName();
    }

    // show short annotation with its values
    String values = Stream.of(type.getDeclaredMethods())
            .map(m -> {
                try {
                    Object val = m.invoke(a);
                    return m.getName() + "=" + String.valueOf(val);
                } catch (Exception e) {
                    return m.getName() + "=<error>";
                }
            })
            .collect(Collectors.joining(", "));

    return "@" + type.getSimpleName() + "(" + values + ")";
}

    public String getClassName() {
        return className;
    }

    public Set<String> getInterceptorBindings() {
        return interceptorBindings;
    }

    @Override
    public String toString() {
        return className + " -> " + interceptorBindings;
    }
}
