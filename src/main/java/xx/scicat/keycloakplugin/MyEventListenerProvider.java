package xx.scicat.keycloakplugin;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

import java.util.List;

public class MyEventListenerProvider implements EventListenerProvider {

    private List<Event> events;

    public MyEventListenerProvider(List<Event> events) {
        this.events = events;
    }

    @Override
    public void onEvent(Event event) {
        events.add(event);
    }

    @Override
    public void close() {

    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // Assume this implementation just ignores admin events
    }
}