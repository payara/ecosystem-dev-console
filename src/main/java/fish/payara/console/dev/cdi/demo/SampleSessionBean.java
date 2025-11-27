/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.cdi.demo;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Gaurav Gupta
 */
@SessionScoped
public class SampleSessionBean implements Serializable {
  
    public static AtomicInteger count = new AtomicInteger();

    @PostConstruct
    public void init() {
        System.out.println("Created instance: " + count.incrementAndGet());
    }
    public String hello() {
        return "hello " + count.get();
    }
    
}
