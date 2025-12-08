/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.core;

import java.lang.reflect.Method;
import net.bytebuddy.asm.Advice;

/**
 *
 * @author Gaurav Gupta
 */
public class DecoratorAdvice {

    @Advice.OnMethodEnter
    static long onEnter(@Advice.Origin Method method,
                        @Advice.FieldValue("registry") DevConsoleRegistry registry,
                        @Advice.FieldValue("beanClass") Class<?> beanClass) {

        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    static void onExit(@Advice.Enter long start,
                       @Advice.Origin Method method,
                       @Advice.FieldValue("registry") DevConsoleRegistry registry,
                       @Advice.FieldValue("beanClass") Class<?> beanClass) {

        long ms = (System.nanoTime() - start) / 1_000_000;
        registry.recordInvocation(beanClass, method.getName(), ms);
    }
}
