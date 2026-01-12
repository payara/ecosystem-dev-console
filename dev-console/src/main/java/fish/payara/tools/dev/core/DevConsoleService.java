/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.tools.dev.core;

import fish.payara.tools.dev.admin.DevConsoleServiceConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 *
 * @author Gaurav Gupta
 */
@Service(name = "dev-console-service")
@RunLevel(StartupRunLevel.VAL)
public class DevConsoleService implements ConfigListener {

    @Inject
    private ServerEnvironment serverEnv;

    @Inject
    ServiceLocator serviceLocator;

    private Boolean devConsoleEnabled;

    private Boolean devConsoleSecure;

    private DevConsoleServiceConfiguration devConsoleServiceConfiguration;

    @PostConstruct
    public void init() {
        devConsoleServiceConfiguration = serviceLocator.getService(DevConsoleServiceConfiguration.class);
    }

    public boolean isEnabled() {
        if (devConsoleEnabled == null) {
            devConsoleEnabled = Boolean.valueOf(devConsoleServiceConfiguration.getEnabled());
        }
        return devConsoleEnabled;
    }
    
    public void resetDevConsoleEnabledProperty() {
        devConsoleEnabled = null;
    }


    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        List<UnprocessedChangeEvent> unchangedList = new ArrayList<>();
        for (PropertyChangeEvent event : events) {
            unchangedList.add(new UnprocessedChangeEvent(event, "Dev Console configuration changed:" + event.getPropertyName()
                    + " was changed from " + event.getOldValue().toString() + " to " + event.getNewValue().toString()));
        }
        return new UnprocessedChangeEvents(unchangedList);
    }
}
