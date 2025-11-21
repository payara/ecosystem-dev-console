/*
 *
 * Copyright (c) 2025 Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/master/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package fish.payara.console.dev.resources;

import fish.payara.console.dev.cdi.dto.BeanDTO;
import fish.payara.console.dev.cdi.dto.BeanGraphDTO;
import fish.payara.console.dev.cdi.dto.SecurityAnnotationDTO;
import fish.payara.console.dev.core.DevConsoleExtension;
import fish.payara.console.dev.core.DevConsoleRegistry;
import fish.payara.console.dev.rest.dto.DecoratorInfo;
import fish.payara.console.dev.rest.dto.EventDTO;
import fish.payara.console.dev.rest.dto.InterceptedClassInfo;
import fish.payara.console.dev.rest.dto.InterceptorInfo;
import fish.payara.console.dev.rest.dto.ObserverDTO;
import fish.payara.console.dev.rest.dto.ProducerDTO;
import fish.payara.console.dev.rest.dto.ProducerInfo;
import fish.payara.console.dev.rest.dto.RestMethodDTO;
import fish.payara.console.dev.rest.dto.RestResourceDTO;
import fish.payara.console.dev.rest.dto.ScopedBeanInfo;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
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

    @Inject
BeanManager beanManager;

private int countBeansByType(String typeName) {
    try {
        Class<?> clazz = Class.forName(typeName);
        Set<Bean<?>> beans = beanManager.getBeans(clazz);
        return beans.size();
    } catch (ClassNotFoundException e) {
        return 0;
    }
}
    @GET
    @Path("/producers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProducerDTO> producers() {
        guard();

        return registry.getProducers().stream()
                .map(info -> {
//                    // Count matching beans currently available
//                    int count = (int) registry.getBeans().stream()
//                            .filter(b -> b.getBeanClass().getName().equals(info.getProducedType()))
//                            .count();

                    return new ProducerDTO(info);
                })
                .toList();
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
                .map(o -> new ObserverDTO(
                o.getObservedType().getTypeName(),
                o.getBeanClass().getName(),
                o.getObservedQualifiers().stream()
                        .map(a -> a.annotationType().getName())
                        .toList(),
                o.getTransactionPhase().name(),
                o.getReception().name()
        ))
                .toList();

        return Response.ok(observerDTOs).build();
    }

    @GET
    @Path("/events")
    public Response getRecentEvents() {
        guard();

        var events = registry.getRecentEvents().stream()
                .map(ev -> new EventDTO(
                ev.getEventType(),
                ev.getFiredBy(),
                ev.getTimestamp(),
                ev.getResolvedObservers()
        ))
                .toList();

        return Response.ok(events).build();
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
