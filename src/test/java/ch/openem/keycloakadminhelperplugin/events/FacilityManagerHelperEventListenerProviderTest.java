package ch.openem.keycloakadminhelperplugin.events;

import ch.openem.keycloakadminhelperplugin.workflow.NewGroupEventHandler;
import org.junit.jupiter.api.Test;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.AuthDetails;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.*;

import static org.mockito.Mockito.*;

class FacilityManagerHelperEventListenerProviderTest {
    public final static String REALM_ID = "xy";
    public final static String GROUP_NAME = "g500";

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
        doReturn(group).when(groupProvider).getGroupById(eq(realm), eq(GROUP_NAME));
    }

    FacilityManagerHelperEventListenerProvider underTest = new FacilityManagerHelperEventListenerProvider(keycloakSession);

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
    void onAdminEvent_noParentAttrib() {
        AdminEvent adminEvent = new AdminEvent();
        adminEvent.setOperationType(OperationType.CREATE);
        adminEvent.setResourceType(ResourceType.GROUP);
        adminEvent.setAuthDetails(new AuthDetails());
        adminEvent.setRepresentation("{\"id\":\"" + GROUP_NAME + "\"}");
        adminEvent.setRealmId(REALM_ID);
        GroupModel groupParent = new GroupModelAdapter();
        group.setParent(groupParent);
        underTest.onEvent(adminEvent, true);
        verifyNoMoreInteractions(handler);
    }

    @Test
    void onAdminEvent_notSubGroup() {
        AdminEvent adminEvent = new AdminEvent();
        adminEvent.setOperationType(OperationType.CREATE);
        adminEvent.setResourceType(ResourceType.GROUP);
        adminEvent.setAuthDetails(new AuthDetails());
        adminEvent.setRepresentation("{\"id\":\"" + GROUP_NAME + "\"}");
        adminEvent.setRealmId(REALM_ID);
        group.setParent(null);
        underTest.onEvent(adminEvent, true);
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
        GroupModel groupParent = new GroupModelAdapter();
        group.setParent(groupParent);
        groupParent.setSingleAttribute(NewGroupEventHandler.FACILITY_NAME_ATTR, "psi");

        underTest.onEvent(adminEvent, true);

        verify(handler).processNewGroupEvent(same(realm), same(group), eq("psi"), same(groupParent));
    }

    @Test
    void testClose() {
        underTest.close(); // no action expected

        verifyNoMoreInteractions(keycloakSession, handler);
    }
}