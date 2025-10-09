
package fish.payara.console.dev.rest.dto;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

public class ScopedBeanInfo {

    private final String beanClass;
    private final String scope;
    private final Set<String> qualifiers;
    private final Set<String> types;
    private final String name;
    private final Set<String> stereotypes;
    private final boolean alternative;
    private final String producedBy; // if created via @Produces

    public ScopedBeanInfo(Bean<?> bean, String producedBy) {
        this.beanClass = bean.getBeanClass().getName();

        Class<?> scopeAnnotation = bean.getScope();
        this.scope = (scopeAnnotation != null) ? scopeAnnotation.getSimpleName() : "Unknown";

        this.qualifiers = bean.getQualifiers().stream()
                .map(a -> formatAnnotation(a))
                .collect(Collectors.toSet());

        this.types = bean.getTypes().stream()
        .filter(t -> {
            // remove Object
            if (t.equals(Object.class)) return false;
            // remove the bean class itself
            if (t.getTypeName().equals(bean.getBeanClass().getName())) return false;
            return true;
        })
        .map(Object::toString)
        .collect(Collectors.toSet());


        this.name = bean.getName();

        this.stereotypes = bean.getStereotypes().stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toSet());

        this.alternative = bean.isAlternative();

        this.producedBy = producedBy;
    }

    private static String formatAnnotation(Annotation a) {
        if (a.annotationType().getDeclaredMethods().length == 0) {
            return "@" + a.annotationType().getSimpleName();
        }
        return "@" + a.annotationType().getSimpleName() + a.toString();
    }

    public String getBeanClass() { return beanClass; }
    public String getScope() { return scope; }
    public Set<String> getQualifiers() { return qualifiers; }
    public Set<String> getTypes() { return types; }
    public String getName() { return name; }
    public Set<String> getStereotypes() { return stereotypes; }
    public boolean isAlternative() { return alternative; }
    public String getProducedBy() { return producedBy; }

    @Override
    public String toString() {
        return beanClass + " @" + scope + " qualifiers=" + qualifiers;
    }
}
