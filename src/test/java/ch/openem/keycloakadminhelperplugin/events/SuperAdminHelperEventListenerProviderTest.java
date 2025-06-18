package ch.openem.keycloakadminhelperplugin.events;

import ch.openem.keycloakadminhelperplugin.workflow.NewGroupEventHandler;
import org.junit.jupiter.api.Test;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.AuthDetails;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SuperAdminHelperEventListenerProviderTest {
    public final static String REALM_ID = "xy";
    public final static String GROUP_NAME = "psi--initnewfacility";

    private final KeycloakSession keycloakSession = mock(KeycloakSession.class);
    NewGroupEventHandler handler = mock(NewGroupEventHandler.class);
    RealmProvider realmProvider = mock(RealmProvider.class);
    GroupProvider groupProvider = mock(GroupProvider.class);
    RealmModel realm = mock(RealmModel.class);
    GroupModel group = new GroupModelAdapter();

    {
        doReturn(realmProvider).when(keycloakSession).realms();
        doReturn(groupProvider).when(keycloakSession).groups();
        doReturn(realm).when(realmProvider).getRealm(eq(REALM_ID));
        doReturn(group).when(groupProvider).getGroupById(eq(realm), any());
    }

    SuperAdminHelperEventListenerProvider underTest = new SuperAdminHelperEventListenerProvider(keycloakSession);

    {
        underTest.handler = handler;
    }

    @Test
    void onEvent() {
        underTest.onEvent(null); // no action expected

        verifyNoMoreInteractions(keycloakSession, handler);
    }

    @Test
    void onAdminEvent_notRelevant1() {
        AdminEvent adminEvent = new AdminEvent();
        adminEvent.setOperationType(OperationType.CREATE);
        adminEvent.setResourceType(ResourceType.AUTH_FLOW);
        underTest.onEvent(adminEvent, true);
        verifyNoMoreInteractions(keycloakSession, handler);
    }

    @Test
    void onAdminEvent_notRelevant2() {
        AdminEvent adminEvent = new AdminEvent();
        adminEvent.setOperationType(OperationType.UPDATE);
        adminEvent.setResourceType(ResourceType.GROUP);
        underTest.onEvent(adminEvent, true);
        verifyNoMoreInteractions(keycloakSession, handler);
    }

    @Test
    void onAdminEvent_noFacilityName() {
        final String GROUP_NAME = "--initnewfacility";

        AdminEvent adminEvent = new AdminEvent();
        adminEvent.setOperationType(OperationType.CREATE);
        adminEvent.setResourceType(ResourceType.GROUP);
        adminEvent.setAuthDetails(new AuthDetails());
        adminEvent.setRepresentation("{\"id\":\"" + GROUP_NAME + "\"}");
        adminEvent.setRealmId(REALM_ID);
        group.setName(GROUP_NAME);
        group.setParent(null);

        assertThrows(IllegalArgumentException.class, () -> underTest.onEvent(adminEvent, true));

        verifyNoMoreInteractions(handler);
    }

    @Test
    void onAdminEvent() {
        AdminEvent adminEvent = new AdminEvent();
        adminEvent.setOperationType(OperationType.CREATE);
        adminEvent.setResourceType(ResourceType.GROUP);
        adminEvent.setAuthDetails(new AuthDetails());
        adminEvent.setRepresentation("{\"id\":\"" + GROUP_NAME + "\"}");
        adminEvent.setRealmId(REALM_ID);
        group.setName(GROUP_NAME);
        group.setParent(null);

        underTest.onEvent(adminEvent, true);

        verify(handler).processNewInitializerGroupEvent(same(realm), same(group), eq("psi"));
    }

    @Test
    void testClose() {
        underTest.close(); // no action expected

        verifyNoMoreInteractions(keycloakSession, handler);
    }
}