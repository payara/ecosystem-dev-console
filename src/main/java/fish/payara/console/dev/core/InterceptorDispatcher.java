/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.core;

import jakarta.interceptor.AroundInvoke;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import net.bytebuddy.asm.Advice.AllArguments;
import net.bytebuddy.asm.Advice.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

/**
 *
 * @author Gaurav Gupta
 */
public class InterceptorDispatcher {

    private final Object target;
    private final DevConsoleRegistry registry;
    private final Class<?> beanClass;

    public InterceptorDispatcher(Object target, DevConsoleRegistry registry, Class<?> beanClass) {
        this.target = target;
        this.registry = registry;
        this.beanClass = beanClass;
    }

    @RuntimeType
    public Object intercept(@Origin Method method,
                            @AllArguments Object[] args,
                            @SuperCall Callable<?> superCall) throws Exception {

        if (!method.isAnnotationPresent(AroundInvoke.class)) {
            return superCall.call();
        }

        long start = System.nanoTime();
        try {
            return superCall.call();
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            registry.recordInvocation(beanClass, ms);
        }
    }
}
