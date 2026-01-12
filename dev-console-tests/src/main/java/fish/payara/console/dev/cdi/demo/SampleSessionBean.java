/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.cdi.demo;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Gaurav Gupta
 */
@SessionScoped
public class SampleSessionBean implements Serializable {

    public static AtomicInteger count = new AtomicInteger();

    private static final int[] SLEEP_OPTIONS = {5, 10, 15};
    private static final Random RAND = new Random();

    public static void randomShortSleep() {
        int ms = SLEEP_OPTIONS[RAND.nextInt(SLEEP_OPTIONS.length)];
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    @PostConstruct
    public void init() {
        randomShortSleep();
        System.out.println("Created instance: " + count.incrementAndGet());
    }

    public String hello() {
        return "hello " + count.get();
    }

}
