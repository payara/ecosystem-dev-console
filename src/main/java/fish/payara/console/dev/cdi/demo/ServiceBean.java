package fish.payara.console.dev.cdi.demo;

import jakarta.enterprise.context.Dependent;

@Dependent
public class ServiceBean implements Service {

    @Override
    public String serve(String input) {
        return "ServiceBean serving: " + input;
    }
}