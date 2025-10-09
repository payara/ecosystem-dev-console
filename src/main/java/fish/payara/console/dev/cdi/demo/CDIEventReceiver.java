package fish.payara.console.dev.cdi.demo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;

@ApplicationScoped
public class CDIEventReceiver {

    public void receiveMessage(@Observes String message) {
        System.out.println("Received message: " + message);
    }

    public void receiveMessageAsync(@ObservesAsync String message) {
        System.out.println("Received message: " + message);
    }
}
