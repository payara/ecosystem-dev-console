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

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import java.util.Set;

/**
 *
 * @author Gaurav Gupta
 */
public class WrappingInjectionTarget<T> implements InjectionTarget<T> {

    private final InjectionTarget<T> delegate;
    private final DevConsoleRegistry registry;
    private final Class<T> beanClass;
    private static final ThreadLocal<Long> creationStart = new ThreadLocal<>();

    WrappingInjectionTarget(Class<T> beanClass,
            InjectionTarget<T> delegate,
            DevConsoleRegistry registry) {
        this.beanClass = beanClass;
        this.delegate = delegate;
        this.registry = registry;
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        if (registry.enabled()) {
            creationStart.set(System.nanoTime());
        }
        T instance = delegate.produce(ctx);
        return instance;
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx) {
        delegate.inject(instance, ctx);
    }

    @Override
    public void postConstruct(T instance) {
        delegate.postConstruct(instance);

        Long start = creationStart.get();
        if (start != null) {
            long end = System.nanoTime();
            long ms = (end - start) / 1_000_000;
            registry.recordCreation(beanClass, ms);
            creationStart.remove();
        }
    }

    @Override
    public void preDestroy(T instance) {
        Long start = System.nanoTime();
        delegate.preDestroy(instance);
        if (registry.enabled()) {
            long end = System.nanoTime();
            long ms = (end - start) / 1_000_000;
            registry.recordDestruction(beanClass, ms);
        }
    }

    @Override
    public void dispose(T instance) {
        delegate.dispose(instance);
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return delegate.getInjectionPoints();
    }
}
