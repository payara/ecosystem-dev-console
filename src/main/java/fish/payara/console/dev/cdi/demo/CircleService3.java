package fish.payara.console.dev.cdi.demo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CircleService3 {
    
    @Inject
    private CircleService1 service;


    public String doSomething() {
        System.out.println("CircleService3: Doing something important...");
        return "Done";
    }

}
