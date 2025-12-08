/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.core;

import jakarta.interceptor.AroundInvoke;
import java.lang.reflect.Method;
import net.bytebuddy.asm.Advice;

/**
 *
 * @author Gaurav Gupta
 */
public class AroundInvokeAdvice {

    @Advice.OnMethodEnter
    static long onEnter(@Advice.Origin Method method, 
                        @Advice.FieldValue("registry") DevConsoleRegistry registry,
                        @Advice.FieldValue("beanClass") Class<?> beanClass) {

        // Only measure methods annotated with @AroundInvoke
        if (!method.isAnnotationPresent(AroundInvoke.class)) {
            return -1L; // flag: do not measure
        }

        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    static void onExit(@Advice.Enter long start,
                       @Advice.Origin Method method,
                       @Advice.FieldValue("registry") DevConsoleRegistry registry,
                       @Advice.FieldValue("beanClass") Class<?> beanClass) {

        if (start == -1L) {
            return; // skip non-AroundInvoke
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        registry.recordInvocation(beanClass, ms);
    }
}
