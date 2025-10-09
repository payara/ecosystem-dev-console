package fish.payara.console.dev.cdi.demo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class CDIEventSender {

    @Inject
    private Event<String> messageEvent;

    public void sendMessage(String message) {
        messageEvent.fire(message);
    }
}
