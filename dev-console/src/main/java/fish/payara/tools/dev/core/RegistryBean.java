package fish.payara.tools.dev.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

@ApplicationScoped
@DevConsoleQualifier
public class RegistryBean implements Bean<DevConsoleRegistry> {

    private final DevConsoleRegistry registry;

    public RegistryBean(DevConsoleRegistry registry) {
        this.registry = registry;
    }

    @Override public Class<?> getBeanClass() { return DevConsoleRegistry.class; }
    @Override public Set<InjectionPoint> getInjectionPoints() { return Set.of(); }
    @Override public DevConsoleRegistry create(CreationalContext<DevConsoleRegistry> ctx) { return registry; }
    @Override public void destroy(DevConsoleRegistry instance, CreationalContext<DevConsoleRegistry> ctx) { }
    @Override public Set<Type> getTypes() { return Set.of(DevConsoleRegistry.class, Object.class); }
    @Override public Set<Annotation> getQualifiers() { return Set.of(DevConsoleQualifier.Literal.INSTANCE); }
    @Override public Class<? extends Annotation> getScope() { return ApplicationScoped.class; }
    @Override public String getName() { return "probeRegistry"; }
    @Override public Set<Class<? extends Annotation>> getStereotypes() { return Set.of(); }
    @Override public boolean isAlternative() { return false; }
}
