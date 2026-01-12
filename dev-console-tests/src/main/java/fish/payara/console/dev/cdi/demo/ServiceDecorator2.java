package fish.payara.console.dev.cdi.demo;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

import java.io.Serializable;

@Decorator
@Priority(10)
public class ServiceDecorator2 implements Service, Serializable {

    @Inject
    @Delegate
    private Service delegate;

    @Override
    public String serve(String input) {
        // Decorator adds extra behavior before delegating
        System.out.println("Decorator: Before serving");
        String result = delegate.serve(input);
        System.out.println("Decorator: After serving");
        return result + " [decorated2]";
    }
}