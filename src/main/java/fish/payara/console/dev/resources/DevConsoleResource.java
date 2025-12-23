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

import fish.payara.console.dev.dto.BeanDTO;
import fish.payara.console.dev.dto.BeanFullDTO;
import fish.payara.console.dev.dto.BeanGraphDTO;
import fish.payara.console.dev.dto.SecurityAnnotationDTO;
import fish.payara.console.dev.core.DevConsoleExtension;
import fish.payara.console.dev.core.DevConsoleRegistry;
import fish.payara.console.dev.dto.DecoratorDTO;
import fish.payara.console.dev.dto.DecoratorFullDTO;
import fish.payara.console.dev.model.InstanceStats;
import fish.payara.console.dev.dto.EventDTO;
import fish.payara.console.dev.dto.InjectionPointDTO;
import fish.payara.console.dev.dto.ObserverDTO;
import fish.payara.console.dev.dto.ProducerDTO;
import fish.payara.console.dev.model.ProducerInfo;
import fish.payara.console.dev.dto.RestMethodDTO;
import fish.payara.console.dev.dto.RestResourceDTO;
import fish.payara.console.dev.dto.InterceptorDTO;
import fish.payara.console.dev.dto.InterceptorFullDTO;
import fish.payara.console.dev.model.DecoratedClassInfo;
import fish.payara.console.dev.model.HTTPRecord;
import fish.payara.console.dev.model.InterceptedClassInfo;
import fish.payara.console.dev.model.ResolutionStatus;
import fish.payara.console.dev.model.ScopedBeanInfo;
import fish.payara.console.dev.rest.RestMetricsRegistry;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/dev")
@Produces(MediaType.APPLICATION_JSON)
public class DevConsoleResource {

    private DevConsoleRegistry registry = DevConsoleExtension.registry;

    @Inject
    private BeanManager beanManager;

    @Inject
    private RestMetricsRegistry restregistry;

    @GET
    @Path("/beans")
    public Object beans() {
        guard();
        return registry.getBeans().stream().map(BeanDTO::new).toList();
    }

    @GET
    @Path("/beans/{id}")
    public Response getBeanById(@PathParam("id") String id) {
        guard();
        for (Bean<?> bean : registry.getBeans()) {
            if (bean.getBeanClass().getName().equals(id)) {
                BeanFullDTO dto = new BeanFullDTO(bean,
                        registry.getStats(bean.getBeanClass()),
                        registry.findProducerForBean(bean.getBeanClass())
                                .map(info -> info.getMemberSignature())
                                .orElse(null)
                );
                return Response.ok(dto).build();
            }
        }
        throw new NotFoundException();
    }

    @GET
    @Path("/scoped-beans")
    public List<ScopedBeanInfo> scopedBeans() {
        guard();

        return registry.getBeans().stream()
                .map(bean -> {

                    var info = new ScopedBeanInfo(bean,
                            registry.findProducerForBean(bean.getBeanClass())
                                    .map(ProducerInfo::getMemberSignature)
                                    .orElse(null));

                    InstanceStats stats
                            = registry.getStats(bean.getBeanClass());

                    info.setCreatedCount(stats.getCreatedCount().get());
                    info.setLastCreated(stats.getLastCreated().get());
                    info.setCurrentCount(stats.getCurrentCount().get());
                    info.setMaxCount(stats.getMaxCount().get());
                    info.setDestroyedCount(stats.getDestroyedCount().get());

                    return info;
                })
                .toList();
    }

    @GET
    @Path("/scoped-beans-detail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response scopedBeansDetailed() {
        guard();

        // Group beans by declared scope (treat null as Dependent)
        Map<Class<? extends Annotation>, List<Bean<?>>> beansByScope = registry.getBeans().stream()
                .collect(Collectors.groupingBy(b -> {
                    Class<? extends Annotation> sc = b.getScope();
                    return sc != null ? sc : jakarta.enterprise.context.Dependent.class;
                }));

        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<Class<? extends Annotation>, List<Bean<?>>> e : beansByScope.entrySet()) {
            Class<? extends Annotation> scopeClass = e.getKey();

            // Skip Dependent scope if you don't want to report it (change as desired)
            if (scopeClass == jakarta.enterprise.context.Dependent.class) {
                continue;
            }

            String scopeKey = "@" + scopeClass.getSimpleName(); // e.g. @RequestScoped
            List<Bean<?>> beanList = e.getValue();

            int beanCount = beanList.size();

            Integer scopeInstancesSum = 0; // null if context not active

            Context context = null;
            boolean contextActive = true;
            try {
                // Try to obtain the context for this scope; may throw ContextNotActiveException
                context = beanManager.getContext(scopeClass);
            } catch (ContextNotActiveException ex) {
                contextActive = false;
            } catch (Throwable ex) {
                // Some implementations may throw different exceptions; treat as not active
                contextActive = false;
            }

            if (!contextActive) {
                // context is not active during this request -> set instances to null
                scopeInstancesSum = null;
            } else {
                // context active: check contextual instance presence for each bean
                for (Bean<?> bean : beanList) {
                    Integer instancesForBean = 0;
                    try {
                        @SuppressWarnings("unchecked")
                        Contextual<Object> contextual = (Contextual<Object>) bean;
                        Object instance = context.get(contextual);
                        if (instance != null) {
                            instancesForBean = 1; // CDI context stores single contextual instance per bean
                        } else {
                            instancesForBean = 0;
                        }
                    } catch (Throwable t) {
                        // defensive: if anything goes wrong, mark as unknown (null)
                        instancesForBean = null;
                    }

                    if (instancesForBean != null && instancesForBean > 0) {
                        scopeInstancesSum = scopeInstancesSum + instancesForBean;
                    }
                }
            }

            Map<String, Object> scopeSummary = new LinkedHashMap<>();
            scopeSummary.put("beanCount", beanCount);
            scopeSummary.put("instances", scopeInstancesSum);

            result.put(scopeKey, scopeSummary);
        }

        return Response.ok(result).build();
    }

    @GET
    @Path("/producers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProducerDTO> producers() {
        guard();

        return registry.getProducers().stream()
                .map(info -> {
                    return new ProducerDTO(info);
                })
                .toList();
    }

    @GET
    @Path("/interceptors")
    public List<InterceptorDTO> interceptors() {
        guard();

        return registry.getInterceptors().stream()
                .map(bean -> {

                    InterceptorDTO info = new InterceptorDTO(bean);

                    InstanceStats stats
                            = registry.getStats(bean.getClassName());

                    info.setCreatedCount(stats.getCreatedCount().get());
                    info.setLastCreated(stats.getLastCreated().get());
                    info.setInvokedCount(stats.getInvocationCount().get());
                    info.setLastInvoked(stats.getLastInvoked().get());

                    return info;
                })
                .toList();
    }

    @GET
    @Path("/interceptors/{className}")
    public InterceptorFullDTO getInterceptor(@PathParam("className") String className) {
        guard();

        // Find interceptor metadata in registry
        var bean = registry.getInterceptors().stream()
                .filter(i -> i.getClassName().equals(className))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                "Interceptor not found: " + className));

        // Prepare DTO
        InterceptorFullDTO info = new InterceptorFullDTO(bean);

        InstanceStats stats = registry.getStats(bean.getClassName());
        if (stats != null) {
            info.setCreatedCount(stats.getCreatedCount().get());
            info.setLastCreated(stats.getLastCreated().get());
            info.setInvokedCount(stats.getInvocationCount().get());
            info.setLastInvoked(stats.getLastInvoked().get());
            info.setInvocationRecords(stats.getInvocationRecords());
        }
        return info;
    }

    @GET
    @Path("/intercepted-classes")
    public List<InterceptedClassInfo> interceptedClasses() {

        guard();

        return interceptorSummary(registry.getInterceptorChains());
    }

    @GET
    @Path("/decorators")
    public List<DecoratorDTO> decorators() {
        guard();
        return registry.getDecorators().stream()
                .map(bean -> {

                    DecoratorDTO info = new DecoratorDTO(bean);

                    InstanceStats stats
                            = registry.getStats(bean.getClassName());

                    info.setCreatedCount(stats.getCreatedCount().get());
                    info.setLastCreated(stats.getLastCreated().get());
                    info.setInvokedCount(stats.getInvocationCount().get());
                    info.setLastInvoked(stats.getLastInvoked().get());

                    return info;
                })
                .toList();
    }

    @GET
    @Path("/decorators/{className}")
    public DecoratorFullDTO getDecorator(@PathParam("className") String className) {
        guard();

        // Find interceptor metadata in registry
        var bean = registry.getDecorators().stream()
                .filter(i -> i.getClassName().equals(className))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                "Decorator not found: " + className));

        // Prepare DTO
        DecoratorFullDTO info = new DecoratorFullDTO(bean);

        InstanceStats stats = registry.getStats(bean.getClassName());
        if (stats != null) {
            info.setCreatedCount(stats.getCreatedCount().get());
            info.setLastCreated(stats.getLastCreated().get());
            info.setInvokedCount(stats.getInvocationCount().get());
            info.setLastInvoked(stats.getLastInvoked().get());
            info.setInvocationRecords(stats.getInvocationRecords());
        }
        return info;
    }

    @GET
    @Path("/decorated-classes")
    public List<DecoratedClassInfo> decoratedClasses() {

        guard();

        return decoratorSummary(registry.getDecoratorChains());
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
    @Path("/bean-graph/{id}")
    public Response getBeanGraphNodeById(@PathParam("id") String id) {
        guard();

        BeanGraphDTO graph = registry.getBeanGraph();
        if (graph == null || graph.getNodes() == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Bean graph not available")
                    .build();
        }

        BeanGraphDTO.BeanNode root = graph.getNodes().get(id);
        if (root == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Bean with id '" + id + "' not found")
                    .build();
        }

        // Build a subgraph containing only this node + dependencies
        Map<String, BeanGraphDTO.BeanNode> subgraphNodes = new LinkedHashMap<>();
        collectRecursive(
                root,
                graph.getNodes(),
                subgraphNodes,
                new HashSet<>()
        );

        BeanGraphDTO subgraph = new BeanGraphDTO();

        // add nodes without creating new objects
        subgraphNodes.forEach((beanId, beanNode) -> {
            subgraph.addNode(beanId, beanNode.getDescription());

            // COPY circular flag
            BeanGraphDTO.BeanNode subNode = subgraph.getNodes().get(beanId);
            subNode.setCircular(beanNode.isCircular());
        });

        // add edges
        subgraphNodes.forEach((beanId, beanNode) -> {
            for (BeanGraphDTO.BeanNode dep : beanNode.getDependencies()) {
                if (subgraphNodes.containsKey(dep.getBeanId())) {
                    subgraph.addDependency(beanId, dep.getBeanId());
                }
            }
        });

        return Response.ok(subgraph).build();
    }

    private void collectRecursive(
            BeanGraphDTO.BeanNode node,
            Map<String, BeanGraphDTO.BeanNode> allNodes,
            Map<String, BeanGraphDTO.BeanNode> result,
            Set<String> visiting) {

        String id = node.getBeanId();

        // Cycle detected
        if (visiting.contains(id)) {
            node.setCircular(true);
            return;
        }

        if (result.containsKey(id)) {
            return;
        }

        visiting.add(id);
        result.put(id, node);

        for (BeanGraphDTO.BeanNode dep : node.getDependencies()) {
            collectRecursive(dep, allNodes, result, visiting);

            // Propagate cycle flag upwards if needed
            if (dep.isCircular()) {
                node.setCircular(true);
            }
        }

        visiting.remove(id);
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

        return registry.getRestMethodInfoMap().values().stream()
                .peek(v -> {
                    int count = restregistry.getMetrics()
                            .getOrDefault(v.getMethodSignature(), List.of())
                            .size();
                    v.setInvoked(count);
                })
                .collect(Collectors.toList());
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
        return registry.getSecurityAnnotations()
                .entrySet().stream()
                .map(e -> new SecurityAnnotationDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Return full details for a rest method. Tries to lookup runtime records
     * from a RestMetricsRegistry bean if present in the CDI container. The
     * {path} parameter is matched against the stored methodSignature
     * (declaringClass#methodName) or the registered REST path.
     *
     * @param path
     * @return
     */
    @GET
    @Path("/rest-methods/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRestMethodFullByPath(@PathParam("path") String path) {
        guard();

        // find RestMethodDTO by matching either methodSignature or the configured path
        RestMethodDTO found = registry.getRestMethodInfoMap().values().stream()
                .filter(m -> (m.getMethodSignature() != null && m.getMethodSignature().equals(path))
                || (m.getPath() != null && m.getPath().equals(path)))
                .findFirst().orElse(null);

        if (found == null) {
            throw new NotFoundException();
        }

        // Build full DTO
        fish.payara.console.dev.dto.RestMethodFullDTO full = new fish.payara.console.dev.dto.RestMethodFullDTO(
                found.getMethodSignature(), found.getPath(), found.getHttpMethodAndProduces());

        List<HTTPRecord> records = restregistry.getMetrics().get(found.getMethodSignature());
        full.setRecords(records);

        return Response.ok(full).build();
    }

    private void guard() {
        if (!registry.enabled()) {
            throw new NotFoundException();
        }
    }

    private Map<String, Map<String, List<Class<?>>>> groupByClass(Map<String, List<Class<?>>> chains) {
        Map<String, Map<String, List<Class<?>>>> grouped = new HashMap<>();

        chains.forEach((key, chain) -> {
            String className;
            String methodName = null;

            int idx = key.indexOf('#');
            if (idx > 0) {
                className = key.substring(0, idx);
                methodName = key.substring(idx + 1);
            } else {
                className = key;
            }

            grouped
                    .computeIfAbsent(className, k -> new LinkedHashMap<>())
                    .put(methodName, chain); // methodName = null means class-level
        });

        return grouped;
    }

    private List<InterceptedClassInfo> interceptorSummary(Map<String, List<Class<?>>> chains) {

        Map<String, Map<String, List<Class<?>>>> grouped = groupByClass(chains);

        List<InterceptedClassInfo> result = new ArrayList<>();

        grouped.forEach((className, methodMap) -> {

            // remove empty chains BEFORE processing
            Map<String, List<Class<?>>> nonEmpty
                    = methodMap.entrySet().stream()
                            .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                            .collect(LinkedHashMap::new,
                                    (m, e) -> m.put(e.getKey(), e.getValue()),
                                    LinkedHashMap::putAll);

            // If nothing left after removing empties → skip entire class
            if (nonEmpty.isEmpty()) {
                return;
            }

            // Only method-level entries (methodName != null)
            Map<String, List<Class<?>>> methodOnly
                    = nonEmpty.entrySet().stream()
                            .filter(e -> e.getKey() != null)
                            .collect(LinkedHashMap::new,
                                    (m, e) -> m.put(e.getKey(), e.getValue()),
                                    LinkedHashMap::putAll);

            // CASE A: no method-level entries → class-only chain
            if (methodOnly.isEmpty()) {
                List<Class<?>> classChain = nonEmpty.values().iterator().next();
                result.add(new InterceptedClassInfo(className, toNames(classChain)));
                return;
            }

            // Distinct interceptor sets across methods
            Set<List<Class<?>>> distinct = new HashSet<>(methodOnly.values());

            // CASE B: all methods share same chain → class-only
            if (distinct.size() == 1) {
                List<Class<?>> chain = distinct.iterator().next();
                result.add(new InterceptedClassInfo(className, toNames(chain)));
                return;
            }

            // CASE C: methods differ → per-method output
            methodOnly.forEach((methodName, chain) -> {
                result.add(new InterceptedClassInfo(
                        className + "#" + methodName,
                        toNames(chain)
                ));
            });
        });

        return result;
    }

    private List<DecoratedClassInfo> decoratorSummary(Map<String, List<Class<?>>> chains) {

        List<DecoratedClassInfo> result = new ArrayList<>();

        chains.forEach((className, chain) -> {

            // skip empty
            if (chain == null || chain.isEmpty()) {
                return;
            }

            // decorators apply to whole class only
            result.add(new DecoratedClassInfo(
                    className,
                    toNames(chain)
            ));
        });

        return result;
    }

    private List<String> toNames(List<Class<?>> chain) {
        return chain.stream().map(Class::getName).toList();
    }

    @GET
    @Path("/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetadata() {
        guard();

        Map<String, Object> meta = new LinkedHashMap<>();

        // core CDI metadata
        meta.put("beanCount", registry.getBeans().size());
        meta.put("scopedBeanCount", registry.getBeans().stream()
                .map(Bean::getScope)
                .filter(s -> s != null)
                .count());

        // interceptors
        meta.put("interceptorCount", registry.getInterceptors().size());
        meta.put("interceptedClassesCount", interceptorSummary(registry.getInterceptorChains()).size());

        // decorators
        meta.put("decoratorCount", registry.getDecorators().size());
        meta.put("decoratedClassesCount", decoratorSummary(registry.getDecoratorChains()).size());

        // producers
        meta.put("producerCount", registry.getProducers().size());

        // REST
        meta.put("restResourceCount", registry.getRestResourcePaths().size());
        meta.put("restMethodCount", registry.getRestMethodInfoMap().size());
        meta.put("restExceptionMapperCount", registry.getRestExceptionMappers().size());

        // observers / events
        meta.put("observerCount", registry.getObservers().size());
        meta.put("recentEventCount", registry.getRecentEvents().size());

        // security
        meta.put("securityAnnotationCount", registry.getSecurityAnnotations().size());

        // extensions
        meta.put("extensionCount",
                registry.getBeans().stream()
                        .map(Bean::getBeanClass)
                        .filter(Extension.class::isAssignableFrom)
                        .count());

        return Response.ok(meta).build();
    }

    @GET
    @Path("/injection-points")
    @Produces(MediaType.APPLICATION_JSON)
    public List<InjectionPointDTO> getAllInjectionPoints() {
        guard();

        return registry.getAllInjectionPoints()
                .values()
                .stream()
                .flatMap(List::stream)
                .map(InjectionPointDTO::new)
                .toList();
    }

    @GET
    @Path("/beans/{id}/injection-points")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInjectionPointsForBean(@PathParam("id") String beanClass) {
        guard();

        var list = registry.getInjectionPointsForBean(beanClass);

        if (list == null || list.isEmpty()) {
            return Response.ok(List.of()).build();
        }

        return Response.ok(
                list.stream()
                        .map(InjectionPointDTO::new)
                        .toList()
        ).build();
    }

    @GET
    @Path("/injection-points/problems")
    @Produces(MediaType.APPLICATION_JSON)
    public List<InjectionPointDTO> getProblematicInjectionPoints() {
        guard();

        return registry.getAllInjectionPoints()
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(ip
                        -> ip.getResolutionStatus() != ResolutionStatus.RESOLVED
                )
                .map(InjectionPointDTO::new)
                .toList();
    }

    @GET
    @Path("/injection-points/unsatisfied")
    @Produces(MediaType.APPLICATION_JSON)
    public List<InjectionPointDTO> getUnsatisfiedInjectionPoints() {
        guard();
        return getByStatus(ResolutionStatus.UNSATISFIED);
    }

    @GET
    @Path("/injection-points/ambiguous")
    @Produces(MediaType.APPLICATION_JSON)
    public List<InjectionPointDTO> getAmbiguousInjectionPoints() {
        guard();
        return getByStatus(ResolutionStatus.AMBIGUOUS);
    }

    @GET
    @Path("/injection-points/not-processed")
    @Produces(MediaType.APPLICATION_JSON)
    public List<InjectionPointDTO> getNotProcessedInjectionPoints() {
        guard();
        return getByStatus(ResolutionStatus.NOT_PROCESSED);
    }

    private List<InjectionPointDTO> getByStatus(ResolutionStatus status) {
        return registry.getAllInjectionPoints()
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(ip -> ip.getResolutionStatus() == status)
                .map(InjectionPointDTO::new)
                .toList();
    }

}
