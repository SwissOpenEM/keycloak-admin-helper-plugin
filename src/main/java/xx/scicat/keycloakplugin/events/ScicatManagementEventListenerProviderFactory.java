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

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.util.Set;

/**
 * @author <a href="mailto:jessy.lenne@stadline.com">Jessy Lennee</a>
 */
public class ScicatManagementEventListenerProviderFactory implements EventListenerProviderFactory {
    private static final Logger LOG = Logger.getLogger(ScicatManagementEventListenerProviderFactory.class);

    private Set<EventType> excludedEvents;
    private Set<OperationType> excludedAdminOperations;
    private String serverUri;
    private String username;
    private String password;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
//        session.authenticationSessions().


        final AuthorizationProvider authorization = session.getProvider(AuthorizationProvider.class);
//         final ResourceServer resourceServer=authorization.getStoreFactory().getPolicyStore().;

        return new ScicatManagementEventListenerProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
        /*
        The following config is only an example. Right now it's not clear where these values can be set in the UI, and if it's possible to add a descriptive text for the UI.

        The values are not used right now
         */
        String DEFAULT_POL = """
                {
                  "type": "user",
                  "name": "abc",
                  "description": "",
                  "decisionStrategy": "UNANIMOUS",
                  "logic": "POSITIVE",
                  "users": ["-ignored-"],
                  ]
                }""";
        String DEFAULT_PERM = """
                {
                  "resources": ["-ignored-"],
                  "policies": ["-ignored-"],
                  "scopes": [
                    "view-members",
                    "manage-membership",
                    "manage-members",
                    "view",
                    "manage"
                  ],
                  "name": "all on %FACILITY%",
                  "description": "bla bla %FACILITY% bla",
                  "resourceType": "Groups"
                }""";
        config.get("example-json-policy-template", DEFAULT_POL);
        config.get("example-json-permission-template", DEFAULT_PERM);
        config.getBoolean("example-next-time-bootstrap-realm", false);

//        serverUri = config.get("serverUri", "http://nginx/frontend_dev.php/webhook/keycloak");
//        username = config.get("username", null);
//        password = config.get("password", null);
        LOG.warn("Event listener installed");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "scicat-groupcreator";
    }

}
