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

import static ch.openem.keycloakadminhelperplugin.workflow.NewGroupEventHandler.FACILITY_NAME_ATTR;
import static java.util.Objects.requireNonNull;

/**
 * Listens on create-group events of subgroups
 */
public class FacilityManagerHelperEventListenerProvider extends AbstractGroupEventProvider {

    protected FacilityManagerHelperEventListenerProvider(KeycloakSession session) {
        super(session);
    }

    NewGroupEventHandler handler;

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

        LOG.debugv("Found group: {0} {1}  isSubGroup={2}", group.toString(), group.getName(), group.getParent() != null);

        if (handler == null)
            handler = new NewGroupEventHandler(session);

        if (group.getParent() == null) {
            LOG.info("abort: is a top level group");
            return;
        }
        final GroupModel topGroup = getTopGroup(group);
        LOG.debug("  - top group is: " + topGroup + " " + topGroup.getName());
        final String facilityName = getFacilityFromGroup(topGroup);
        if (facilityName == null) {
            LOG.info("abort: no " + FACILITY_NAME_ATTR + " attribute set on top level group");
            return;
        }

        handler.processNewGroupEvent(realm, group, facilityName, topGroup);
    }


    @Override
    public void close() {
    }
}
