package fish.payara.console.dev.core;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import java.util.Set;

class WrappingInjectionTarget<T> implements InjectionTarget<T> {
    private final InjectionTarget<T> delegate;
    private final DevConsoleRegistry registry;

    WrappingInjectionTarget(InjectionTarget<T> delegate, DevConsoleRegistry registry) {
        this.delegate = delegate;
        this.registry = registry;
    }

    @Override public T produce(CreationalContext<T> ctx) {
        T instance = delegate.produce(ctx);
        if (!registry.enabled()) return instance;
        return instance; // hook for wrapping/proxy if needed
    }
    @Override public void inject(T instance, CreationalContext<T> ctx) { delegate.inject(instance, ctx); }
    @Override public void postConstruct(T instance) { delegate.postConstruct(instance); }
    @Override public void preDestroy(T instance) { delegate.preDestroy(instance); }
    @Override public void dispose(T instance) { delegate.dispose(instance); }
    @Override public Set<InjectionPoint> getInjectionPoints() { return delegate.getInjectionPoints(); }
}
