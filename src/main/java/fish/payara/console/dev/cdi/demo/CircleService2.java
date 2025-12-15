package fish.payara.console.dev.cdi.demo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CircleService2 {
    
    @Inject
    private CircleService3 service;


    public String doSomething() {
        System.out.println("CircleService2: Doing something important...");
        return "Done";
    }

}
