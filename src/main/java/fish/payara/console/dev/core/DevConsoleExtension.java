package fish.payara.console.dev.core;

import fish.payara.console.dev.rest.dto.ProducerInfo;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.*;
import jakarta.ws.rs.ext.ExceptionMapper;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class DevConsoleExtension implements Extension {

    public static final DevConsoleRegistry registry = new DevConsoleRegistry();

    <X, T> void onProcessProducerField(@Observes ProcessProducerField<X, T> ppf, BeanManager bm) {
        ProducerInfo info = new ProducerInfo(
                ppf.getAnnotatedProducerField(),
                ppf.getAnnotatedProducerField().getBaseType(),
                ProducerInfo.Kind.FIELD, bm
        );
        System.out.println("Captured Producer Field: " + info);
        registry.addProducer(info);
    }

    <X, T> void onProcessProducerMethod(@Observes ProcessProducerMethod<X, T> ppm, BeanManager bm) {
        ProducerInfo info = new ProducerInfo(
                ppm.getAnnotatedProducerMethod(),
                ppm.getAnnotatedProducerMethod().getBaseType(),
                ProducerInfo.Kind.METHOD, bm
        );
        System.out.println("Captured Producer Method: " + info);
        registry.addProducer(info);
    }

    <T> void onProcessAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
        if (pat.getAnnotatedType().isAnnotationPresent(jakarta.decorator.Decorator.class)) {
            registry.addDecorator(pat.getAnnotatedType());
        }
        // Check for security annotations on REST resources
        if (pat.getAnnotatedType().isAnnotationPresent(jakarta.annotation.security.RolesAllowed.class)
                || pat.getAnnotatedType().isAnnotationPresent(jakarta.annotation.security.PermitAll.class)
                || pat.getAnnotatedType().isAnnotationPresent(jakarta.annotation.security.DenyAll.class)) {
            registry.addSecurityAnnotation(pat.getAnnotatedType());
        }

        pat.getAnnotatedType().getMethods().forEach(m -> {
            if (m.isAnnotationPresent(jakarta.annotation.security.RolesAllowed.class)
                    || m.isAnnotationPresent(jakarta.annotation.security.PermitAll.class)
                    || m.isAnnotationPresent(jakarta.annotation.security.DenyAll.class)) {
                registry.addSecurityAnnotation(m);
            }
        });

        registry.seenType(pat.getAnnotatedType());

        AnnotatedType<T> at = pat.getAnnotatedType();
        if (ExceptionMapper.class.isAssignableFrom(at.getJavaClass())) {
            System.out.println("Found REST exception-mapper: " + at.getJavaClass().getName());
            Class<? extends Throwable> exceptionType = getExceptionType(at.getJavaClass());
            System.out.printf("Found REST exception-mapper: %s (for %s)%n",
                    at.getJavaClass().getName(), exceptionType.getName());
            registry.addRestExceptionMapper(at, exceptionType);
        }

        final String classLevelPath;
        if (at.isAnnotationPresent(jakarta.ws.rs.Path.class)) {
            System.out.println("Found REST resource: " + at.getJavaClass().getName());
            jakarta.ws.rs.Path pathAnnotation = at.getAnnotation(jakarta.ws.rs.Path.class);
            if (pathAnnotation != null) {
                classLevelPath = pathAnnotation.value();
                registry.addRestResourcePath(at, classLevelPath);
            } else {
                classLevelPath = null;
            }
        } else {
            classLevelPath = null;
        }

        at.getMethods().forEach(m -> {
            if (m.isAnnotationPresent(jakarta.ws.rs.GET.class)
                    || m.isAnnotationPresent(jakarta.ws.rs.POST.class)
                    || m.isAnnotationPresent(jakarta.ws.rs.PUT.class)
                    || m.isAnnotationPresent(jakarta.ws.rs.DELETE.class)
                    || m.isAnnotationPresent(jakarta.ws.rs.OPTIONS.class)
                    || m.isAnnotationPresent(jakarta.ws.rs.PATCH.class)
                    || m.isAnnotationPresent(jakarta.ws.rs.HEAD.class)) {
                System.out.println(" u007F REST endpoint method: " + m.getJavaMember());

                // Store the rest method path if present
                jakarta.ws.rs.Path methodPath = m.getAnnotation(jakarta.ws.rs.Path.class);
                String combinedPath = null;
                if (classLevelPath != null) {
                    if (methodPath != null) {
                        combinedPath = classLevelPath + (methodPath.value().startsWith("/") ? "" : "/") + methodPath.value();
                    } else {
                        combinedPath = classLevelPath;
                    }
                } else if (methodPath != null) {
                    combinedPath = methodPath.value();
                }

                // Retrieve produces media type if present
                jakarta.ws.rs.Produces producesAnnotation = m.getAnnotation(jakarta.ws.rs.Produces.class);
                String produces = producesAnnotation != null ? String.join(",", producesAnnotation.value()) : null;

                String httpMethod = null;
                if (m.isAnnotationPresent(jakarta.ws.rs.GET.class)) {
                    httpMethod = "GET";
                } else if (m.isAnnotationPresent(jakarta.ws.rs.POST.class)) {
                    httpMethod = "POST";
                } else if (m.isAnnotationPresent(jakarta.ws.rs.PUT.class)) {
                    httpMethod = "PUT";
                } else if (m.isAnnotationPresent(jakarta.ws.rs.DELETE.class)) {
                    httpMethod = "DELETE";
                } else if (m.isAnnotationPresent(jakarta.ws.rs.OPTIONS.class)) {
                    httpMethod = "OPTIONS";
                } else if (m.isAnnotationPresent(jakarta.ws.rs.PATCH.class)) {
                    httpMethod = "PATCH";
                } else if (m.isAnnotationPresent(jakarta.ws.rs.HEAD.class)) {
                    httpMethod = "HEAD";
                }
                registry.addRestMethodPathWithProduces(m, combinedPath, produces, httpMethod);
            }
        });
    }

    public static Class<? extends Throwable> getExceptionType(Class<?> mapperClass) {
        for (Type type : mapperClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType parameterizedType) {
                if (parameterizedType.getRawType() == ExceptionMapper.class) {
                    Type arg = parameterizedType.getActualTypeArguments()[0];
                    if (arg instanceof Class<?>) {
                        return (Class<? extends Throwable>) arg;
                    }
                }
            }
        }
        // fallback if not found (e.g. if it implements indirectly)
        return Throwable.class;
    }

    <T> void onProcessBeanAttributes(@Observes ProcessBeanAttributes<T> pba) {
        if (pba.getAnnotated().isAnnotationPresent(jakarta.interceptor.Interceptor.class)) {
            registry.addInterceptor(pba.getAnnotated());
        }
    }

    <T> void onProcessBean(@Observes ProcessBean<T> pb) {
//        Class<?> clazz = pb.getBean().getBeanClass();
//        if (!isFromCurrentWar(clazz)) {
//            return; // skip external classed
//        } 
        registry.registerBean(pb.getBean());//pb.getBean().getBeanClass()
    }

    <T, X> void onProcessObserver(@Observes ProcessObserverMethod<T, X> pom) {
        registry.registerObserver(pom.getObserverMethod());
    }

    <T> void onProcessInjectionTarget(@Observes ProcessInjectionTarget<T> pit) {
        if (!registry.enabled()) {
            return;
        }
        InjectionTarget<T> delegate = pit.getInjectionTarget();
        pit.setInjectionTarget(new WrappingInjectionTarget<>(delegate, registry));
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        if (!registry.enabled()) {
            return;
        }
        abd.addBean(new RegistryBean(registry));
    }

    void afterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager bm) {
        if (!registry.enabled()) {
            return;
        }
        registry.finishModel(bm);
    }

    private boolean isFromCurrentWar(Class<?> clazz) {
        ClassLoader warLoader = Thread.currentThread().getContextClassLoader();
        return clazz.getClassLoader() == warLoader;
    }

}
