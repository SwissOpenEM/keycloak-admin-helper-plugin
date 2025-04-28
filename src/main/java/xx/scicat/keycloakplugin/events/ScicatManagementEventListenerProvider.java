/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xx.scicat.keycloakplugin.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static xx.scicat.keycloakplugin.events.NewGroupEventHandler.FACILITY_NAME_ATTR;

/**
 * @author <a href="mailto:jessy.lenne@stadline.com">Jessy Lenne</a>
 */
public class ScicatManagementEventListenerProvider implements EventListenerProvider {
    private static final Logger LOG = Logger.getLogger(ScicatManagementEventListenerProvider.class);
    private final KeycloakSession session;

    public ScicatManagementEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        // normal events not of interest
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        if (event.getOperationType() == OperationType.CREATE && event.getResourceType() == ResourceType.GROUP) {
            handleNewGroupEvent(event);
        } else {
            LOG.tracev("Ignore event without binding: {0}, {1}", event.getOperationType(), event.getResourceType());
        }
    }

    private void handleNewGroupEvent(AdminEvent event) {
        dumpEvent(event);

        final RealmModel realm = session.realms().getRealm(event.getRealmId());
        final String newGroupId = requireNonNull(getGroupIdFromEvent(event));
        final GroupModel group = requireNonNull(session.groups().getGroupById(realm, newGroupId));

        LOG.warnv("Found group: {0} {1}  isSubGroup={2}", group.toString(), group.getName(), group.getParent() != null);
        if (group.getParent() == null) {
            LOG.warn("abort: already a top level group");
            return;
        }
        final GroupModel topGroup = getTopGroup(group);
        LOG.warn("  - top group is: " + topGroup + " " + topGroup.getName());
        final String facilityName = getFacilityFromGroup(topGroup);
        if (facilityName == null) {
            LOG.warn("abort: no " + FACILITY_NAME_ATTR + " attribute set on top level group");
            return;
        }

        NewGroupEventHandler handler = new NewGroupEventHandler(session);
        handler.processNewGroupEvent(realm, group, facilityName, topGroup);
    }

    private void dumpEvent(AdminEvent event) {
        LOG.warn("ADMIN EVENT on realm " + event.getRealmName() + ": " + toString(event));
        LOG.warn("ADMIN EVENT: " + event.getRepresentation());
        LOG.warn("ADMIN EVENT: " + event.getResourceType());
        if (event.getDetails() != null)
            event.getDetails().forEach((key, value) -> LOG.warn("ADMIN EVENT:   " + key + " " + value));
    }

    private static String getGroupIdFromEvent(AdminEvent event) {
        try {
            EventRepresentationLight rep = JsonSerialization.readValue(event.getRepresentation(), EventRepresentationLight.class);
            return rep.id;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFacilityFromGroup(String name) {
        if (!name.contains("-")) return null;
        return name.split("-")[0];
    }

    private String getFacilityFromGroup(GroupModel group) {
        return group.getFirstAttribute(FACILITY_NAME_ATTR);
    }

    private GroupModel getTopGroup(GroupModel group) {
        while (group.getParent() != null) {
            group = group.getParent();
        }
        return group;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventRepresentationLight {
        @JsonProperty
        public String id;
    }

    private String toString(AdminEvent adminEvent) {
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

    @Override
    public void close() {
    }
}
