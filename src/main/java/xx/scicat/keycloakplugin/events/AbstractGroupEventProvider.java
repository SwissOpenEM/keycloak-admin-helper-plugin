package xx.scicat.keycloakplugin.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.logging.Logger;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;

import static xx.scicat.keycloakplugin.workflow.NewGroupEventHandler.FACILITY_NAME_ATTR;

public abstract class AbstractGroupEventProvider implements EventListenerProvider {
    final Logger LOG = Logger.getLogger(getClass());
    final KeycloakSession session;

    protected AbstractGroupEventProvider(KeycloakSession session) {
        this.session = session;
    }

    static String getGroupIdFromEvent(AdminEvent event) {
        try {
            EventRepresentationLight rep = JsonSerialization.readValue(event.getRepresentation(), EventRepresentationLight.class);
            return rep.id;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static GroupModel getTopGroup(GroupModel group) {
        while (group.getParent() != null) {
            group = group.getParent();
        }
        return group;
    }

    void dumpEvent(AdminEvent event) {
        LOG.warn("ADMIN EVENT on realm " + event.getRealmName() + ": " + toString(event));
        LOG.warn("ADMIN EVENT: " + event.getRepresentation());
        LOG.warn("ADMIN EVENT: " + event.getResourceType());
        if (event.getDetails() != null)
            event.getDetails().forEach((key, value) -> LOG.warn("ADMIN EVENT:   " + key + " " + value));
    }

    String toString(AdminEvent adminEvent) {
        StringBuilder sb = new StringBuilder();

        sb.append("{\"type\": \"");
        sb.append(adminEvent.getOperationType());
        sb.append("\", \"realmId\": \"");
        sb.append(adminEvent.getAuthDetails().getRealmId());
        sb.append("\", \"clientId\": \"");
        sb.append(adminEvent.getAuthDetails().getClientId());
        sb.append("\", \"userId\": \"");
        sb.append(adminEvent.getAuthDetails().getUserId());
        sb.append("\", \"ipAddress\": \"");
        sb.append(adminEvent.getAuthDetails().getIpAddress());
        sb.append("\", \"resourcePath\": \"");
        sb.append(adminEvent.getResourcePath());
        sb.append("\"");

        if (adminEvent.getError() != null) {
            sb.append(", \"error\": \"");
            sb.append(adminEvent.getError());
            sb.append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    String getFacilityFromGroup(GroupModel group) {
        return group.getFirstAttribute(FACILITY_NAME_ATTR);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventRepresentationLight {
        @JsonProperty
        public String id;
    }
}
