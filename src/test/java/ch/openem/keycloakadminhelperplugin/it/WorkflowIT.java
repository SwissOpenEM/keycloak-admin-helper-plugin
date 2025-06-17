package ch.openem.keycloakadminhelperplugin.it;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static ch.openem.keycloakadminhelperplugin.it.TestConstants.KEYCLOAK_HTTP_PORT;
import static ch.openem.keycloakadminhelperplugin.it.TestConstants.REALM_TEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
class WorkflowIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowIT.class);
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = KeyCloakContainer.createContainer()
            .withExposedPorts(KEYCLOAK_HTTP_PORT)
            .withLogConsumer(new Slf4jLogConsumer(LOGGER).withSeparateOutputStreams())
            .withRealmImportFile("/realm-integrationtest-old.json")
            // .withRealmImportFile("/realm-integrationtest.json")  - doesn't start but only contains +adminPermissionsEnabled+policy+permission
            .withStartupTimeout(Duration.ofSeconds(90));
    private static String KEYCLOAK_AUTH_URL;

    @BeforeAll
    static void setUp() {
        KEYCLOAK_AUTH_URL = KEYCLOAK_CONTAINER.getAuthServerUrl();
        LOGGER.info("Running test with Keycloak image: " + KeyCloakContainer.get());
    }

    private static Keycloak keycloakAdmin() {
        return TestConstants.keycloakAdmin(KEYCLOAK_AUTH_URL);
    }

    private static Keycloak keycloakTest(String username, String password, String client) {
        return TestConstants.keycloakTest(KEYCLOAK_AUTH_URL, username, password, client, null);
    }

    private static Keycloak keycloakTest(String username, String password, String client, String clientSecret) {
        return TestConstants.keycloakTest(KEYCLOAK_AUTH_URL, username, password, client, clientSecret);
    }

    private void switchAccessProvider(String accessProviderId) {
        try (Keycloak admin = keycloakAdmin()) {
            AuthenticationManagementResource flows = admin.realm(REALM_TEST).flows();
            String authenticationConfigId = flows
                    .getExecutions("direct-grant-restrict-client-auth").stream()
                    .filter(it -> it.getProviderId().equalsIgnoreCase("restrict-client-auth-authenticator"))
                    .findFirst()
                    .get()
                    .getAuthenticationConfig();
            AuthenticatorConfigRepresentation authenticatorConfig = flows.getAuthenticatorConfig(
                    authenticationConfigId);
            Map<String, String> config = authenticatorConfig.getConfig();
            if (accessProviderId == null) {
                config.remove("accessProviderId");
            } else {
                config.put("accessProviderId", accessProviderId);
            }
            authenticatorConfig.setConfig(config);
            flows.updateAuthenticatorConfig(authenticationConfigId, authenticatorConfig);
        }
    }

    @Nested
    class FullWorkflow {
        public static final String REALM_NAME = "it-fullworkflow";

//        @BeforeEach
//        void switchAccessProvider() {
//            SuperUserIT.this.switchAccessProvider("policy");
//        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void superAdmin_and_FacilityManager_workflow(boolean alsoTextFacilityManager) {
            try (Keycloak keycloak = keycloakAdmin()) {
                // clean up if necessary
                cleanupRealm(keycloak, REALM_NAME);

                // create realm including AdminPermissionsEnabled
                RealmResource realm = createRealm(keycloak, REALM_NAME);

                // set event listeners. take both to make sure there are no side-effects
                RealmEventsConfigRepresentation eventRep = realm.getRealmEventsConfig();
                eventRep.getEventsListeners().add("scicat-superadmin-helper");
                eventRep.getEventsListeners().add("scicat-facilitymanager-helper");
                realm.updateRealmEventsConfig(eventRep);


                testSuperAdminWorkflow(realm, "abc");

                if (alsoTextFacilityManager) {
                    // do this test to make sure that the "produced" facility works for the facility manager
                    testMinimalFacilityManagerWorkflow(realm, "abc");
                }
            }
        }

        @Test
        @Disabled
        void facilityManager_workflow_withImportedRealm() {
            try (Keycloak keycloak = keycloakAdmin()) {
                RealmResource realm = keycloak.realm("integrationtest");

                testSuperAdminWorkflow(realm, "abc");

                testMinimalFacilityManagerWorkflow(realm, "psi");
            }
        }

        private static void testSuperAdminWorkflow(RealmResource realm, String facilityName) {
            // create croup
            GroupRepresentation groupRep = new GroupRepresentation();
            groupRep.setName(facilityName + "--initnewfacility");
            realm.groups().add(groupRep).close();

            //wait
            await().until(() ->
                    realm.groups()
                            .query(facilityName + "--initnewfacility")
                            .stream()
                            .noneMatch(q -> (facilityName + "--initnewfacility").equals(q.getName())));

            // get group information
            List<GroupRepresentation> groupResult = realm.groups().query(facilityName, false, 0, 100, false);
            assertEquals(1, groupResult.size());

            // expect: group is renamed
            assertEquals(facilityName, groupResult.get(0).getName());
            // expect: group has new attribute
            assertEquals(facilityName, groupResult.get(0).getAttributes().get("facility-name").get(0));

            // get admin user information
            List<UserRepresentation> userResult = realm.users().search(facilityName + "-admin", 0, 100, false);
            assertThat(userResult).hasSize(1);
            assertThat(userResult.get(0).getUsername()).isEqualTo(facilityName + "-admin");
            assertThat(userResult.get(0).isEnabled()).isTrue();

            // expect: user has role
            MappingsRepresentation allUserRoles = realm.users().get(userResult.get(0).getId()).roles().getAll();
            assertThat(allUserRoles.getClientMappings().get("realm-management").getMappings()
                    .stream().map(RoleRepresentation::getName))
                    .contains("view-users");
            // expect: user has attribute / seems that users don't have attributes??
            //                Map<String, List<String>> allUserAttributes = realm.users().get(userResult.get(0).getId()).getUnmanagedAttributes();
            //                assertThat(allUserAttributes.get("facility-name")).isEqualTo(List.of(facilityName));
        }

        private static void testMinimalFacilityManagerWorkflow(RealmResource realm, String facilityName) {
            final String subGroupName = "test" + System.currentTimeMillis();

            // get parent group id
            String parentGroupId = realm.getGroupByPath("/" + facilityName).getId();

            // create subgroup
            GroupRepresentation groupRep = new GroupRepresentation();
            groupRep.setName(subGroupName);
            realm.groups().group(parentGroupId).subGroup(groupRep).close();

            // verify group
            GroupRepresentation groupRepResult = realm.getGroupByPath("/" + facilityName + "/" + subGroupName);
            assertThat(groupRepResult.getName()).isEqualTo(subGroupName);
            assertThat(groupRepResult.getParentId()).isEqualTo(parentGroupId);
            // expect: group has new attribute
            assertThat(groupRepResult.getAttributes().get("facility-name").get(0)).isEqualTo(facilityName);
        }
    }

    private static RealmResource createRealm(Keycloak keycloak, String realmName) {
        RealmRepresentation realmRep = new RealmRepresentation();
        realmRep.setRealm(realmName);
        realmRep.setAdminPermissionsEnabled(true);
        keycloak.realms().create(realmRep);

        return keycloak.realm(realmName);
    }

    private void cleanupRealm(Keycloak keycloak, String realmName) {
        try {
            RealmResource realm = keycloak.realms().realm(realmName);
            realm.remove();
        } catch (NotFoundException e) {
            // this is ok
        }
    }
}
