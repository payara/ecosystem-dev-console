/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.core;

import jakarta.interceptor.AroundInvoke;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @author Gaurav Gupta
 */
public class AroundInvokeInterceptor implements InvocationHandler {

    private final Object target;
    private final DevConsoleRegistry registry;
    private final Class<?> beanClass;

    public AroundInvokeInterceptor(Object target, DevConsoleRegistry registry, Class<?> beanClass) {
        this.target = target;
        this.registry = registry;
        this.beanClass = beanClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (!method.isAnnotationPresent(AroundInvoke.class)) {
            return method.invoke(target, args);
        }

        long start = System.nanoTime();
        try {
            return method.invoke(target, args);
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            registry.recordInvocation(beanClass, ms);
        }
    }
}
