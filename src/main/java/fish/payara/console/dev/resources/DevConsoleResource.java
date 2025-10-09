package fish.payara.console.dev.resources;

import fish.payara.console.dev.cdi.dto.BeanDTO;
import fish.payara.console.dev.cdi.dto.BeanGraphDTO;
import fish.payara.console.dev.cdi.dto.ObserverDTO;
import fish.payara.console.dev.cdi.dto.SecurityAnnotationDTO;
import fish.payara.console.dev.core.DevConsoleExtension;
import fish.payara.console.dev.core.DevConsoleRegistry;
import fish.payara.console.dev.rest.dto.DecoratorInfo;
import fish.payara.console.dev.rest.dto.InterceptedClassInfo;
import fish.payara.console.dev.rest.dto.InterceptorInfo;
import fish.payara.console.dev.rest.dto.ProducerInfo;
import fish.payara.console.dev.rest.dto.RestMethodDTO;
import fish.payara.console.dev.rest.dto.RestResourceDTO;
import fish.payara.console.dev.rest.dto.ScopedBeanInfo;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.interceptor.Interceptor;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/dev")
@Produces(MediaType.APPLICATION_JSON)
public class DevConsoleResource {

    DevConsoleRegistry registry = DevConsoleExtension.registry;

    @GET
    @Path("/beans")
    public Object beans() {
        guard();
        return registry.getBeans().stream().map(BeanDTO::new).toList();
    }

    @GET
    @Path("/scoped-beans")
    public List<ScopedBeanInfo> scopedBeans() {
        guard();

        Set<String> seenKeys = new HashSet<>();
        return registry.getBeans().stream()
                .filter(bean -> {
                    var scope = bean.getScope();
                    return scope != null && (scope.equals(jakarta.inject.Singleton.class)
                            || scope.isAnnotationPresent(jakarta.enterprise.context.NormalScope.class));
                })
                .map(bean -> new ScopedBeanInfo(bean, registry.findProducerForBean(bean.getBeanClass())
                .map(ProducerInfo::getMemberSignature)
                .orElse(null)))
                .filter(beanInfo -> {
                    String key = beanInfo.getBeanClass() + "#" + beanInfo.getScope() + "#" + beanInfo.getName();
                    if (seenKeys.contains(key)) {
                        return false; // skip duplicate
                    } else {
                        seenKeys.add(key);
                        return true;
                    }
                })
                .toList();
    }

    @GET
    @Path("/producers")
    public List<ProducerInfo> producers() {
        guard();
        return registry.getProducers();
    }

    @GET
    @Path("/interceptors")
    public List<InterceptorInfo> interceptors() {
        guard();
        return registry.getInterceptors();
    }

    @GET
    @Path("/intercepted-classes")
    public List<InterceptedClassInfo> interceptedClasses() {
        guard();
        return registry.getBeans().stream()
                .filter(bean -> !bean.getBeanClass().isAnnotationPresent(Interceptor.class)) // exclude interceptor classes
                .filter(this::hasInterceptorBinding) // only beans with interceptor bindings
                .map(InterceptedClassInfo::new)
                .toList();
    }

    @GET
    @Path("/decorators")
    public List<DecoratorInfo> decorators() {
        guard();
        return registry.getDecorators();
    }

    @GET
    @Path("/extension")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getExtensions() {
        guard();
        return registry.getBeans().stream()
                .map(bean -> bean.getBeanClass())
                .filter(Extension.class::isAssignableFrom) // only classes implementing Extension
                .map(Class::getName)
                .distinct()
                .sorted()
                .toList();
    }

    @GET
    @Path("/observers")
    public Response getObservers() {
        guard();
        var observers = registry.getObservers();
        var observerDTOs = observers.stream()
                .map(o -> new ObserverDTO(o))
                .collect(Collectors.toList());
        return Response.ok(observerDTOs).build();
    }

    @GET
    @Path("/seen-types")
    public Response getSeenTypes() {
        guard();
        var seenTypes = registry.getSeenTypes();
        var result = seenTypes.entrySet().stream().map(e -> {
            var valueStrings = e.getValue().stream()
                    .map(Class::getName)
                    .sorted()
                    .toList();
            return new java.util.HashMap<String, Object>() {
                {
                    put("className", e.getKey());
                    put("annotations", valueStrings);
                }
            };
        }).sorted((a, b) -> ((String) a.get("className")).compareTo((String) b.get("className"))).toList();
        return Response.ok(result).build();
    }

    @GET
    @Path("/bean-graph")
    public BeanGraphDTO getBeanGraph() {
        return registry.getBeanGraph();
    }

    @GET
    @Path("/rest-resources")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RestResourceDTO> getRestResources() {
        guard();
        return registry.getRestResourcePaths().entrySet().stream()
                .map(e -> new RestResourceDTO(e.getKey().getJavaClass().getName(), e.getValue()))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/rest-methods")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RestMethodDTO> getRestMethods() {
        guard();
        return registry.getRestMethodInfoMap().values().stream().collect(Collectors.toList());
    }

    @GET
    @Path("/rest-exception-mappers")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getRestExceptionMappers() {
        guard();
        return registry.getRestExceptionMappers();
    }

    @GET
    @Path("/security-audit")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SecurityAnnotationDTO> getSecurityAnnotations() {
        guard();
        return registry.getSecurityAnnotations().entrySet().stream()
                .map(e -> new SecurityAnnotationDTO(e.getKey().toString(), e.getValue()))
                .collect(Collectors.toList());
    }

    @GET
    @Path("deny")
    @Produces(MediaType.APPLICATION_JSON)
    @jakarta.annotation.security.DenyAll
    public void deny() {
        guard();
    }

    private void guard() {
        if (!registry.enabled()) {
            throw new NotFoundException();
        }
    }

    /**
     * Utility: check if a bean has any interceptor binding
     */
    private boolean hasInterceptorBinding(Bean<?> bean) {
        Class<?> beanClass = bean.getBeanClass();
        List<Annotation> annotations = Arrays.asList(beanClass.getAnnotations());

        // Look for any annotation that is meta-annotated with @InterceptorBinding
        return annotations.stream().anyMatch(a
                -> a.annotationType().isAnnotationPresent(jakarta.interceptor.InterceptorBinding.class)
        );
    }
}
