package fish.payara.console.dev.cdi.demo;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExampleService {

    @Logging
    public String doSomething() {
        System.out.println("ExampleService: Doing something important...");
        return "Done";
    }

}