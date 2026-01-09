package fish.payara.console.dev.cdi.demo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Validate
public class ExampleService {
    
    @Inject
    private Service service;

    @Logging
    public String doSomething() {
        System.out.println("ExampleService: Doing something important...");
        return  service.serve("Done");
    }

    public String doSomething2() {
        System.out.println("ExampleService: Doing something important...");
        return "Done";
    }

}
