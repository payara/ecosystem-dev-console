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
package fish.payara.console.dev.core;

import fish.payara.console.dev.model.Record;
import fish.payara.console.dev.model.InstanceStats;
import fish.payara.console.dev.dto.BeanGraphDTO;
import fish.payara.console.dev.model.DecoratorInfo;
import fish.payara.console.dev.model.EventRecord;
import fish.payara.console.dev.model.InterceptorInfo;
import fish.payara.console.dev.model.ProducerInfo;
import fish.payara.console.dev.dto.RestMethodDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 *
 * @author Gaurav Gupta
 */
@ApplicationScoped
public class DevConsoleRegistry {

    private final List<ProducerInfo> producers = new ArrayList<>();
    private final Map<Object, Set<Annotation>> securityAnnotations = new ConcurrentHashMap<>();
    private final List<DecoratorInfo> decorators = new ArrayList<>();
    private final List<InterceptorInfo> interceptors = new ArrayList<>();

    private final boolean enabled
            = Boolean.parseBoolean(System.getProperty("payara.dev.console", "true"));

    private final Map<String, Bean<?>> beans = new ConcurrentHashMap<>();
    private final Map<ObserverMethod<?>, String> observerMethodNames = new ConcurrentHashMap<>();
    private final List<ObserverMethod<?>> observers = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, String> restExceptionMappers = Collections.synchronizedMap(new LinkedHashMap<>());
    private volatile BeanManager bm;
    private final Map<String, Set<Class<? extends Annotation>>> seenTypes = new ConcurrentHashMap<>();
    private final BeanGraphDTO beanGraph = new BeanGraphDTO();

    // Event monitoring structures
    private static final int DEFAULT_EVENT_HISTORY = 1000;
    private final Deque<EventRecord> recentEvents = new ConcurrentLinkedDeque<>();

    private final ConcurrentHashMap<Class<?>, InstanceStats> statsMap = new ConcurrentHashMap<>();

    public void recordCreation(Class<?> beanClass, long ms) {
        InstanceStats stats = statsMap.computeIfAbsent(beanClass, c -> new InstanceStats());
        int live = stats.getCurrentCount().incrementAndGet();
        stats.getCreatedCount().incrementAndGet();
        stats.getLastCreated().set(Instant.now());
        stats.getMaxCount().updateAndGet(prevMax -> Math.max(prevMax, live));
        stats.getCreationRecords().add(new Record(Instant.now(), ms));
    }

    public void recordDestruction(Class<?> beanClass, long ms) {
        InstanceStats stats = statsMap.get(beanClass);
        if (stats != null) {
            stats.getCurrentCount().decrementAndGet();
            stats.getDestroyedCount().incrementAndGet();
            stats.getDestructionRecords().add(new Record(Instant.now(), ms));
        }
    }

    public InstanceStats getStats(Class<?> beanClass) {
        return statsMap.getOrDefault(beanClass, new InstanceStats());
    }

    public int getCurrent(Class<?> beanClass) {
        return getStats(beanClass).getCurrentCount().get();
    }

    public int getMax(Class<?> beanClass) {
        return getStats(beanClass).getMaxCount().get();
    }

    public int getDestroyed(Class<?> beanClass) {
        return getStats(beanClass).getDestroyedCount().get();
    }

    public int getTotalCreated(Class<?> beanClass) {
        return getStats(beanClass).getCreatedCount().get();
    }

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
        beans.put(bean.getBeanClass().getName(), bean);
        // Add bean as node in the graph
        beanGraph.addNode(bean.getBeanClass().getName(), bean.getBeanClass().getName(), bean.toString());
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
            String httpMethodAndProduces = (httpMethod != null ? httpMethod : "") + (produces != null ? " (produces: " + produces + ")" : "");
            String methodName = am.getJavaMember().getName();
            String declaringClass = am.getDeclaringType().getJavaClass().getName();
            String methodId = declaringClass + "#" + methodName;
            RestMethodDTO restMethodDTO = new RestMethodDTO(methodId, path, httpMethodAndProduces);
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

   /**
     * Preferred register method used at ProcessObserverMethod time: supplies both the
     * ObserverMethod and the AnnotatedMethod (if available) so we can remember the method name.
     */
    void registerObserver(ObserverMethod<?> om, jakarta.enterprise.inject.spi.AnnotatedMethod<?> annotatedMethod) {
        observers.add(om);
        if (annotatedMethod != null && annotatedMethod.getJavaMember() != null) {
            try {
                observerMethodNames.put(om, annotatedMethod.getJavaMember().getName());
            } catch (Throwable t) {
                // defensive: some implementations may provide odd AnnotatedMethod instances
            }
        }
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
            String fromId = bean.getBeanClass().getName();
            // Check injection points
            for (InjectionPoint ip : bean.getInjectionPoints()) {
                Bean<?> injectedBean = bm.resolve(bm.getBeans(ip.getType(), ip.getQualifiers().toArray(new Annotation[0])));
                if (injectedBean != null) {
                    String toId = injectedBean.getBeanClass().getName();
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

    /**
     * Register a fired event into the in-memory history.  The caller (usually an observer bean) should compute
     * the list of resolved observer method strings (bean#method) before calling this.
     * @param eventType
     * @param firedBy
     * @param timestamp
     * @param resolvedObservers
     */
    public void registerEvent(String eventType, String firedBy, Instant timestamp, List<String> resolvedObservers) {
        EventRecord rec = new EventRecord(eventType, firedBy, timestamp, resolvedObservers);
        recentEvents.addLast(rec);
        // trim history
        while (recentEvents.size() > DEFAULT_EVENT_HISTORY) {
            recentEvents.removeFirst();
        }
    }

    public List<EventRecord> getRecentEvents() {
        return new ArrayList<>(recentEvents);
    }

    /**
     * Resolve candidate observers for the given event type name using the observer registry collected during bootstrap.
     * This performs a simple name-equality match; you may extend it to handle generics/assignability if required.
     * @param eventTypeName
     * @return 
     */
    public List<String> resolveObserversFor(String eventTypeName) {
        List<String> matches = new ArrayList<>();
        synchronized (observers) {
            for (ObserverMethod<?> om : observers) {
                java.lang.reflect.Type observedType = om.getObservedType();
                String observedTypeName = observedType == null ? null : observedType.getTypeName();
                if (eventTypeName.equals(observedTypeName)) {
                    try {
                        String beanClassName = om.getBeanClass() != null ? om.getBeanClass().getName() : null;
                        String methodName = observerMethodNames.get(om); // use captured name if present
                        if (beanClassName != null && methodName != null) {
                            matches.add(beanClassName + "#" + methodName);
                            continue;
                        } else if (beanClassName != null) {
                            // fallback to beanClass#<unknown>
                            matches.add(beanClassName + "#<unknown>");
                            continue;
                        }
                    } catch (Throwable t) {
                        // ignore and fall-through to fallback
                    }
                    matches.add(om.toString());
                }
            }
        }
        return matches;
    }

}
