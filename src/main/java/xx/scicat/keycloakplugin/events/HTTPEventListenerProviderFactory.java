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
import org.keycloak.authorization.model.ResourceServer;
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
public class HTTPEventListenerProviderFactory implements EventListenerProviderFactory {
    private static final Logger LOG = Logger.getLogger(HTTPEventListenerProviderFactory.class);

    private Set<EventType> excludedEvents;
    private Set<OperationType> excludedAdminOperations;
    private String serverUri;
    private String username;
    private String password;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
//        session.authenticationSessions().


         final AuthorizationProvider authorization=session.getProvider(AuthorizationProvider.class);
//         final ResourceServer resourceServer=authorization.getStoreFactory().getPolicyStore().;

        return new HTTPEventListenerProvider(excludedEvents, excludedAdminOperations, serverUri, username, password,session);
    }

    @Override
    public void init(Config.Scope config) {
//        String[] excludes = config.getArray("exclude-events");
//        if (excludes != null) {
//            excludedEvents = new HashSet<>();
//            for (String e : excludes) {
//                excludedEvents.add(EventType.valueOf(e));
//            }
//        }
//
//        String[] excludesOperations = config.getArray("excludesOperations");
//        if (excludesOperations != null) {
//            excludedAdminOperations = new HashSet<>();
//            for (String e : excludesOperations) {
//                excludedAdminOperations.add(OperationType.valueOf(e));
//            }
//        }
//
//        serverUri = config.get("serverUri", "http://nginx/frontend_dev.php/webhook/keycloak");
//        username = config.get("username", null);
//        password = config.get("password", null);
//
//        System.out.println("Forwarding keycloak events to: " + serverUri);
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
