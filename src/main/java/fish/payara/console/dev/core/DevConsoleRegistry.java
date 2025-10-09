package fish.payara.console.dev.core;

import fish.payara.console.dev.cdi.dto.BeanGraphDTO;
import fish.payara.console.dev.rest.dto.DecoratorInfo;
import fish.payara.console.dev.rest.dto.InterceptorInfo;
import fish.payara.console.dev.rest.dto.ProducerInfo;
import fish.payara.console.dev.rest.dto.RestMethodDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class DevConsoleRegistry {

    private final List<ProducerInfo> producers = new ArrayList<>();
    private final Map<Object, Set<Annotation>> securityAnnotations = new ConcurrentHashMap<>();
    private final List<DecoratorInfo> decorators = new ArrayList<>();
    private final List<InterceptorInfo> interceptors = new ArrayList<>();

    private final boolean enabled
            = Boolean.parseBoolean(System.getProperty("payara.dev.console", "true"));

    private final Map<String, Bean<?>> beans = new ConcurrentHashMap<>();
    private final List<ObserverMethod<?>> observers = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, String> restExceptionMappers = Collections.synchronizedMap(new LinkedHashMap<>());
    private volatile BeanManager bm;
    private final Map<String, Set<Class<? extends Annotation>>> seenTypes = new ConcurrentHashMap<>();
    private final BeanGraphDTO beanGraph = new BeanGraphDTO();

    public void addDecorator(AnnotatedType decorator) {
        decorators.add(DecoratorInfo.fromAnnotatedType(decorator));
    }

    public List<DecoratorInfo> getDecorators() {
        return decorators;
    }

    public void addInterceptor(Annotated interceptor) {
        interceptors.add(InterceptorInfo.fromAnnotated(interceptor));
    }

    public List<InterceptorInfo> getInterceptors() {
        return interceptors;
    }

    public void addProducer(ProducerInfo producer) {
        producers.add(producer);
    }

    public List<ProducerInfo> getProducers() {
        return producers;
    }
    public Optional<ProducerInfo> findProducerForBean(Class<?> beanClass) {
        return producers.stream()
                .filter(p -> p.getProducedType().equals(beanClass.getName()))
                .findFirst();
    }

    public void addSecurityAnnotation(Object element) {
        Set<Annotation> annotations = new HashSet<>();
        if (element instanceof jakarta.enterprise.inject.spi.AnnotatedType) {
            jakarta.enterprise.inject.spi.AnnotatedType<?> at = (jakarta.enterprise.inject.spi.AnnotatedType<?>) element;
            for (Annotation ann : at.getAnnotations()) {
                if (ann.annotationType() == jakarta.annotation.security.RolesAllowed.class
                        || ann.annotationType() == jakarta.annotation.security.PermitAll.class
                        || ann.annotationType() == jakarta.annotation.security.DenyAll.class) {
                    annotations.add(ann);
                }
            }
        } else if (element instanceof jakarta.enterprise.inject.spi.AnnotatedMethod) {
            jakarta.enterprise.inject.spi.AnnotatedMethod<?> am = (jakarta.enterprise.inject.spi.AnnotatedMethod<?>) element;
            for (Annotation ann : am.getAnnotations()) {
                if (ann.annotationType() == jakarta.annotation.security.RolesAllowed.class
                        || ann.annotationType() == jakarta.annotation.security.PermitAll.class
                        || ann.annotationType() == jakarta.annotation.security.DenyAll.class) {
                    annotations.add(ann);
                }
            }
        }
        if (!annotations.isEmpty()) {
            securityAnnotations.put(element, annotations);
        }
    }

    public Map<Object, Set<Annotation>> getSecurityAnnotations() {
        return securityAnnotations;
    }

    public boolean enabled() {
        return enabled;
    }


    void seenType(AnnotatedType<?> at) {
        if (at != null && at.getJavaClass() != null) {
            Set<Class<? extends Annotation>> presentAnnotations = new HashSet<>();
            for (Annotation ann : at.getAnnotations()) {
                presentAnnotations.add(ann.annotationType());
            }
            seenTypes.put(at.getJavaClass().getName(), presentAnnotations);
        }
    }

    void registerBean(Bean<?> bean) {
        beans.put(bean.toString(), bean);
        // Add bean as node in the graph
        beanGraph.addNode(bean.toString(), bean.getBeanClass().getName(), bean.toString());
    }

    private final Map<jakarta.enterprise.inject.spi.AnnotatedType<?>, String> restResourcePaths = new ConcurrentHashMap<>();
    private final Map<jakarta.enterprise.inject.spi.AnnotatedMethod<?>, RestMethodDTO> restMethodInfoMap = new ConcurrentHashMap<>();

    public <T> void addRestResourcePath(jakarta.enterprise.inject.spi.AnnotatedType<T> at, String path) {
        if (at != null && path != null) {
            restResourcePaths.put(at, path);
            String className = at.getJavaClass().getName();
            beanGraph.addNode(className, className, "REST Resource");
        }
    }

    public <T> String getRestResourcePath(jakarta.enterprise.inject.spi.AnnotatedType<T> at) {
        if (at == null) {
            return null;
        }
        return restResourcePaths.get(at);
    }

    public <T> void addRestMethodPathWithProduces(jakarta.enterprise.inject.spi.AnnotatedMethod<? super T> am, String path, String produces, String httpMethod) {
        if (am != null) {
            RestMethodDTO restMethodDTO = new RestMethodDTO();
            restMethodDTO.setPath(path);
            String httpMethodAndProduces = (httpMethod != null ? httpMethod : "") + (produces != null ? " (produces: " + produces + ")" : "");
            restMethodDTO.setHttpMethodAndProduces(httpMethodAndProduces);
            String methodName = am.getJavaMember().getName();

            String declaringClass = am.getDeclaringType().getJavaClass().getName();
            String methodId = declaringClass + "#" + methodName;
            restMethodDTO.setMethodSignature(methodId);
            restMethodInfoMap.put(am, restMethodDTO);
            beanGraph.addNode(methodId, methodName + " " + httpMethodAndProduces, "REST Method");
        }
    }

    public <T> RestMethodDTO getRestMethodInfo(jakarta.enterprise.inject.spi.AnnotatedMethod<? super T> am) {
        if (am == null) {
            return null;
        }
        return restMethodInfoMap.get(am);

    }

    public <T> void addRestExceptionMapper(jakarta.enterprise.inject.spi.AnnotatedType<T> at, Class<? extends Throwable> exceptionType) {
        if (at != null && exceptionType != null) {
            restExceptionMappers.put(at.getBaseType().getTypeName(), exceptionType.getName());
            String className = at.getJavaClass().getName();
            beanGraph.addNode(className, className, "REST ExceptionMapper");
        }
    }

    public Map<String, String> getRestExceptionMappers() {
        return restExceptionMappers;
    }

    void registerObserver(ObserverMethod<?> om) {
        observers.add(om);
    }

    void finishModel(BeanManager bm) {
        this.bm = bm;
        buildBeanDependencyGraph();
    }

    private void buildBeanDependencyGraph() {
        if (bm == null) {
            return;
        }
        for (Bean<?> bean : beans.values()) {
            String fromId = bean.toString();
            // Check injection points
            for (InjectionPoint ip : bean.getInjectionPoints()) {
                Bean<?> injectedBean = bm.resolve(bm.getBeans(ip.getType(), ip.getQualifiers().toArray(new Annotation[0])));
                if (injectedBean != null) {
                    String toId = injectedBean.toString();
                    beanGraph.addDependency(fromId, toId);
                }
            }
        }
    }

    public Collection<Bean<?>> getBeans() {
        return beans.values();
    }

    public List<ObserverMethod<?>> getObservers() {
        return observers;
    }

    public Map<String, Set<Class<? extends Annotation>>> getSeenTypes() {
        return seenTypes;
    }

    public BeanGraphDTO getBeanGraph() {
        return beanGraph;
    }

    public Map<jakarta.enterprise.inject.spi.AnnotatedType<?>, String> getRestResourcePaths() {
        return restResourcePaths;
    }

    public Map<jakarta.enterprise.inject.spi.AnnotatedMethod<?>, RestMethodDTO> getRestMethodInfoMap() {
        return restMethodInfoMap;
    }
}
