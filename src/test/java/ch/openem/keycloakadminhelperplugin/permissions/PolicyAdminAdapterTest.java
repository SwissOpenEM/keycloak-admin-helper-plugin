package ch.openem.keycloakadminhelperplugin.permissions;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.GroupModel;
import org.keycloak.representations.idm.authorization.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PolicyAdminAdapterTest {
    ResourceServer resourceServer = mock(ResourceServer.class);
    AuthorizationProvider authorizationProvider = mock(AuthorizationProvider.class);
    StoreFactory storeFactory = mock(StoreFactory.class);

    {
        doReturn(storeFactory).when(authorizationProvider).getStoreFactory();
    }

    PolicyStore policyStore = mock(PolicyStore.class);

    {
        doReturn(policyStore).when(storeFactory).getPolicyStore();
    }

    PolicyAdminAdapter underTest = new PolicyAdminAdapter(resourceServer, authorizationProvider);

    @Test
    public void createPolicy_new() {
        UserPolicyRepresentation policy = new UserPolicyRepresentation();
        policy.setType("user");
        policy.setName("abc");
        policy.setDescription("");
        policy.setDecisionStrategy(DecisionStrategy.UNANIMOUS);
        policy.setLogic(Logic.POSITIVE);
        policy.addUser("xxxxx-name");
        Policy newlyCreated = mock(Policy.class);
        doReturn(newlyCreated).when(policyStore).create(any(), eq(policy));

        Policy ret = underTest.createPolicy(policy);

        assertSame(newlyCreated, ret);
        verify(policyStore).create(same(resourceServer), eq(policy));
    }

    @Test
    public void createPolicy_exists() {
        UserPolicyRepresentation policy = new UserPolicyRepresentation();
        policy.setType("user");
        final String name = "abc";
        policy.setName(name);
        policy.setDescription("");
        policy.setDecisionStrategy(DecisionStrategy.UNANIMOUS);
        policy.setLogic(Logic.POSITIVE);
        policy.addUser("xxxxx-name");
        Policy existing = mock(Policy.class);
        doReturn(existing).when(policyStore).findByName(any(), eq(name));

        Policy ret = underTest.createPolicy(policy);

        assertSame(existing, ret);
    }

    @Test
    public void createPermission_new() {
        ScopePermissionRepresentation permission = new ScopePermissionRepresentation();
        permission.addResource("6079a169-553b-4e71-b0a6-7cb329f76b6f");
        permission.setPolicies(Set.of("27ac1c9a-57e3-41c8-973b-b5b2849a323e"));
        permission.setScopes(Set.of("view-members", "manage-membership", "manage-members", "view", "manage"));
        permission.setName("all on psi");
        permission.setDescription("bla");
        permission.setResourceType("Groups");

        underTest.createPermission(permission);

        verify(policyStore).create(same(resourceServer), eq(permission));
    }

    @Test
    public void addGroupToExistingPermission_new() {
        ScopePermissionRepresentation permission = new ScopePermissionRepresentation();
        permission.addResource("6079a169-553b-4e71-b0a6-7cb329f76b6f");
        permission.setPolicies(Set.of("27ac1c9a-57e3-41c8-973b-b5b2849a323e"));
        permission.setScopes(Set.of("view-members", "manage-membership", "manage-members", "view", "manage"));
        final String name = "all on psi";
        permission.setName(name);
        permission.setDescription("bla");
        permission.setResourceType("Groups");
        doReturn(null).when(policyStore).findByName(any(), eq(name));

        boolean res = underTest.addGroupToExistingPermission(name, mock(GroupModel.class));

        assertFalse(res);
    }

    @Test
    @Disabled("too complex for mocking")
    public void addGroupToExistingPermission_existing() {
        ScopePermissionRepresentation permission = new ScopePermissionRepresentation();
        permission.addResource("6079a169-553b-4e71-b0a6-7cb329f76b6f");
        permission.setPolicies(Set.of("27ac1c9a-57e3-41c8-973b-b5b2849a323e"));
        permission.setScopes(Set.of("view-members", "manage-membership", "manage-members", "view", "manage"));
        final String name = "all on psi";
        permission.setName(name);
        permission.setDescription("bla");
        permission.setResourceType("Groups");
        GroupModel groupToAdd = mock(GroupModel.class);
        doReturn("123-4").when(groupToAdd).getId();
        doReturn(mock(Policy.class)).when(policyStore).findByName(any(), eq(name));
        PolicyProviderFactory<PolicyRepresentation> providerFactory = mock(PolicyProviderFactory.class);
        doReturn(providerFactory).when(authorizationProvider).getProviderFactory(any());
        PolicyRepresentation representation = new PolicyRepresentation();
        representation.setResources(Set.of("a", "b"));
        doReturn(representation).when(providerFactory).toRepresentation(any(), any());

        // RepresentationToModel.toModel is very hard to mock
        boolean res = underTest.addGroupToExistingPermission(name, groupToAdd);

        assertTrue(res);
    }
}
