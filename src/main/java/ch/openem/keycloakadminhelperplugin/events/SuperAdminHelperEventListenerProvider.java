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

package ch.openem.keycloakadminhelperplugin.events;

import ch.openem.keycloakadminhelperplugin.workflow.NewGroupEventHandler;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import static java.util.Objects.requireNonNull;

/**
 * Listens on create-group events with name suffix "--initnewfacility"
 */
public class SuperAdminHelperEventListenerProvider extends AbstractGroupEventProvider {

    public static final String GROUPNAME_INIT_SUFFIX = "--initnewfacility";

    protected SuperAdminHelperEventListenerProvider(KeycloakSession session) {
        super(session);
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

        final RealmModel realm = requireNonNull(session.realms().getRealm(event.getRealmId()));
        final String newGroupId = requireNonNull(getGroupIdFromEvent(event));
        final GroupModel group = requireNonNull(session.groups().getGroupById(realm, newGroupId));

        String groupName = group.getName();
        if (group.getParent() == null && groupName.endsWith(GROUPNAME_INIT_SUFFIX)) {
            LOG.debugv("Found group: {0} {1}  isSubGroup={2}", group.toString(), group.getName(), group.getParent() != null);

            NewGroupEventHandler handler = new NewGroupEventHandler(session);

            String facilityName = groupName.substring(0, groupName.length() - GROUPNAME_INIT_SUFFIX.length());
            while (facilityName.endsWith("-")) facilityName = facilityName.substring(0, facilityName.length() - 1);
            if (facilityName.isBlank())
                throw new IllegalArgumentException("Facility name in group name " + groupName + " is empty");

            LOG.infov("Initializing a new facility with name {0}", facilityName);

            handler.processNewInitializerGroupEvent(realm, group, facilityName);
        }
    }

    @Override
    public void close() {
    }
}
