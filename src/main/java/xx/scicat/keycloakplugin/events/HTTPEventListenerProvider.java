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
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.Logic;
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;
import org.keycloak.representations.idm.authorization.UserPolicyRepresentation;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:jessy.lenne@stadline.com">Jessy Lenne</a>
 */
public class HTTPEventListenerProvider implements EventListenerProvider {
    private static final Logger LOG = Logger.getLogger(HTTPEventListenerProvider.class);
    public static final String FACILITY_NAME_ATTR = "facility-name";
    //	private final OkHttpClient httpClient = new OkHttpClient();
//    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Set<EventType> excludedEvents;
    private Set<OperationType> excludedAdminOperations;
    private String serverUri;
    private String username;
    private String password;
    private final KeycloakSession session;
    public static final String publisherId = "keycloak";

    public HTTPEventListenerProvider(Set<EventType> excludedEvents, Set<OperationType> excludedAdminOperations, String serverUri, String username, String password, KeycloakSession session) {
        this.excludedEvents = excludedEvents;
        this.excludedAdminOperations = excludedAdminOperations;
        this.serverUri = serverUri;
        this.username = username;
        this.password = password;
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        LOG.warn("EVENT (non-admin): " + toString(event));
//        // Ignore excluded events
//        if (excludedEvents != null && excludedEvents.contains(event.getType())) {
//            return;
//        } else {
//            String stringEvent = toString(event);
//            try {
//
//            	okhttp3.RequestBody jsonRequestBody = okhttp3.RequestBody.create(stringEvent, JSON);
//
//                okhttp3.Request.Builder builder = new Request.Builder()
//                        .url(this.serverUri)
//                        .addHeader("User-Agent", "KeycloakHttp Bot");
//
//                if (this.username != null && this.password != null) {
//                	builder.addHeader("Authorization", this.getAuthHeader());
//                }
//
//                Request request = builder.post(jsonRequestBody)
//                        .build();
//
//            	Response response = httpClient.newCall(request).execute();
//
//            	if (!response.isSuccessful()) {
//            		throw new IOException("Unexpected code " + response);
//            	}
//
//                // Get response body
//                System.out.println(response.body().string());
//            } catch(Exception e) {
//                // ?
//                System.out.println("Failed to forward webhook Event " + e.toString());
//                System.out.println("Request body string: " + stringEvent);
//                e.printStackTrace();
//                return;
//            }
//        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {


        if (event.getOperationType() == OperationType.CREATE && event.getResourceType() == ResourceType.GROUP) {
            LOG.warn("EVENT (admin): " + toString(event));
            LOG.warn("EVENT (admin): " + event.getRepresentation());
            LOG.warn("EVENT (admin): " + event.getResourceType());
            if (event.getDetails() != null)
                event.getDetails().forEach((key, value) -> LOG.warn("EVENT (admin):   " + key + " " + value));

//            RealmModel realm = session.getContext().getRealm();
            final RealmModel realm = session.realms().getRealm(event.getRealmId());
//            GroupModel group = session.groups().getGroupById(realm, event.getId());
//            if (group != null) {
//                LOG.warn("Found group: " + group + " " + group.getName());
//            }

//            String resPath = event.getResourcePath();
//            if (resPath.startsWith("groups/")) {
//                String resId = resPath.split("/")[1];
//                GroupModel group2 = session.groups().getGroupById(realm, resId);
//                if (group2 != null) {
//                    LOG.warn("Found top group(2): " + group2 + " " + group2.getName());
//                }
//            }

            String newGroupId = requireNonNull(getGroupIdFromEvent(event));
            GroupModel group = requireNonNull(session.groups().getGroupById(realm, newGroupId));

            LOG.warnv("Found group: {0} {1}  issubgroup={2}", group, group.getName(), group.getParent() != null);
            if (group.getParent() == null) {
                LOG.warn("abort: already a top level group");
                return;
            }
            GroupModel topGroup = getTopGroup(group);
            LOG.warn("  - top group is: " + topGroup + " " + topGroup.getName());
            String facilityName = getFacilityFromGroup(topGroup);
            if (facilityName == null) {
                LOG.warn("abort: no " + FACILITY_NAME_ATTR + " attribute set on top level group");
                return;
            }

            processNewGroupEvent(realm, group, facilityName, topGroup);

        }
//
//        // Ignore excluded operations
//        if (excludedAdminOperations != null && excludedAdminOperations.contains(event.getOperationType())) {
//            return;
//        } else {
//            String stringEvent = toString(event);
//
//
//            try {
//            	okhttp3.RequestBody jsonRequestBody = okhttp3.RequestBody.create(stringEvent, JSON);
//
//                okhttp3.Request.Builder builder = new Request.Builder()
//                        .url(this.serverUri)
//                        .addHeader("User-Agent", "KeycloakHttp Bot");
//
//                if (this.username != null && this.password != null) {
//                	builder.addHeader("Authorization", this.getAuthHeader());
//                }
//
//                Request request = builder.post(jsonRequestBody)
//                        .build();
//
//            	Response response = httpClient.newCall(request).execute();
//
//            	if (!response.isSuccessful()) {
//            		throw new IOException("Unexpected code " + response);
//            	}
//
//                // Get response body
//                System.out.println(response.body().string());
//            } catch(Exception e) {
//                // ?
//                System.out.println("Failed to forward webhook AdminEvent " + e.toString());
//                System.out.println("Request body string: " + stringEvent);
//                e.printStackTrace();
//                return;
//            }
//        }
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

    private void processNewGroupEvent(RealmModel realm, GroupModel group, String facilityName, GroupModel topGroup) {
        LOG.warnv("Detected a new group in realm {0}: {1} {2} issubgroup={3}", realm.getName(), group, group.getName(), group.getParent() != null);

        String newName = findCollisionFreeGroupName(realm, group, facilityName);
        if (!group.getName().equals(newName)) {
            LOG.warnv("  * no or bad prefix -> rename group to {}", newName);
            group.setName(newName);
        }
        LOG.warnv("  * make group top-level and set {} attribute", FACILITY_NAME_ATTR);
        group.setParent(null);
        group.setSingleAttribute(FACILITY_NAME_ATTR, facilityName);


//        session.clientPolicy().


        LOG.warn("--mark1--");

//        ClientModel clientModel = requireNonNull(realm.getClientByClientId("realm-management"));
//        ClientModel clientModel = requireNonNull(realm.getClientById("25c12e6b-407f-4eb3-af90-b89a70ae77cb")); // clientModel admin-permissions@29e4ac91,admin-permissions,null,25c12e6b-407f-4eb3-af90-b89a70ae77cb,null,null,null,null,null
        ClientModel clientModel = realm.getAdminPermissionsClient();
        LOG.warn("clientModel " + clientModel + "," + clientModel.getClientId() + "," + clientModel.getName() + "," + clientModel.getId() + "," + clientModel.getType() + "," + clientModel.getBaseUrl() + "," + clientModel.getDescription() + "," + clientModel.getManagementUrl() + "," + clientModel.getRootUrl());

        AuthorizationProvider authz = requireNonNull(session.getProvider(AuthorizationProvider.class));
        StoreFactory storeFactory = requireNonNull(authz.getStoreFactory());
//        StoreFactory storeFactory = session.getProvider(StoreFactory.class);
        ResourceServer resourceServer = requireNonNull(storeFactory.getResourceServerStore().findByClient(clientModel));
        PolicyStore policyStore = storeFactory.getPolicyStore();
        CreatePol cp = new CreatePol(resourceServer, authz);


        {
            LOG.warn("--mark2--");

            UserPolicyRepresentation policyRep = new UserPolicyRepresentation();
            policyRep.setType("user");
            policyRep.setName(facilityName + " admin users policy");
            policyRep.setDescription("");
            policyRep.setDecisionStrategy(DecisionStrategy.UNANIMOUS);
            policyRep.setLogic(Logic.POSITIVE);
            policyRep.addUser(facilityName + "-admin");
            Policy policy = cp.createPolicy(policyRep);


            {
                ScopePermissionRepresentation permissionRep = new ScopePermissionRepresentation();
                permissionRep.setResourceType("Groups");
                permissionRep.addResource(group.getId());
                permissionRep.setPolicies(Set.of(policy.getId()));
                permissionRep.setScopes(Set.of("view-members", "manage-membership", "manage-members", "view", "manage"));
                permissionRep.setName(facilityName + " allow permission for " + group.getId());
                permissionRep.setDescription("bla");
                Policy permission = cp.createPermission(permissionRep);
            }
        }


//        Policy policyToBeEdited = policyStore.findByName(resourceServer, oldPolicyName);
//        AbstractPolicyRepresentation representation = ModelToRepresentation.toRepresentation(policyToBeEdited, authz);
//        representation.setName(newPolicyName);
//        representation.setId(policyToBeEdited.getId());
//
//        RepresentationToModel.toModel(representation, authz, policyToBeEdited);
//
//
//        AdminAuth adminAuth = new AdminAuth(realm, auth.getToken(), auth.getUser(), auth.getClient());
//        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, adminAuth, session, session.getContext().getConnection());
//        session.getTransactionManager().commit();
//        adminEvent.operation(OperationType.UPDATE).resourcePath(authz.getKeycloakSession().getContext().getUri()).representation(representation).success();


    }

    private String findCollisionFreeGroupName(RealmModel realm, GroupModel group, String facilityName) {
        String newNameUnnumbered = group.getName();
        if (!newNameUnnumbered.startsWith(facilityName + "-")) {
            newNameUnnumbered = facilityName + "-" + newNameUnnumbered;
        }
        String newName = newNameUnnumbered;
        for (int i = 1; ; i++) {
            if (session.groups().getGroupByName(realm, null, newName) == null
                    && session.groups().getGroupByName(realm, group.getParent(), newName) == null)
                break;
            newName = newNameUnnumbered + i;
        }
        return newName;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventRepresentationLight {
        @JsonProperty
        public String id;
    }

//    private String getAuthHeader() {
//        String token = this.username + ":" + this.password;
//        String encodedToken = Base64.getEncoder().encodeToString(token.getBytes());
//        return "Basic " + encodedToken;
//    }

    private String toString(Event event) {
        StringBuilder sb = new StringBuilder();

        sb.append("{\"type\": \"");
        sb.append(event.getType());
        sb.append("\", \"realmId\": \"");
        sb.append(event.getRealmId());
        sb.append("\", \"clientId\": \"");
        sb.append(event.getClientId());
        sb.append("\", \"userId\": \"");
        sb.append(event.getUserId());
        sb.append("\", \"ipAddress\": \"");
        sb.append(event.getIpAddress());
        sb.append("\"");

        if (event.getError() != null) {
            sb.append(", \"error\": \"");
            sb.append(event.getError());
            sb.append("\"");
        }
        sb.append(", \"details\": {");
        if (event.getDetails() != null) {
            int i = 0;
            for (Map.Entry<String, String> e : event.getDetails().entrySet()) {
                sb.append("\"");
                sb.append(e.getKey());
                sb.append("\": \"");
                sb.append(e.getValue());
                sb.append("\"");
                if (i < event.getDetails().size() - 1) {
                    sb.append(", ");
                }
                i = i + 1;
            }
        }
        sb.append("}}");

        return sb.toString();
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
