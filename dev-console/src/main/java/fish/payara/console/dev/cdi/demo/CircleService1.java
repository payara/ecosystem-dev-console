package fish.payara.console.dev.cdi.demo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CircleService1 {
    
    @Inject
    private CircleService2 service;


    public String doSomething() {
        System.out.println("CircleService1: Doing something important...");
        return "Done";
    }

}
