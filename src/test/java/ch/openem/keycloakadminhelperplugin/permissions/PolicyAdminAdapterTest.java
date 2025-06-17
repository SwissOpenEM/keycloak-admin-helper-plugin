package ch.openem.keycloakadminhelperplugin.permissions;

import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.Logic;
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;
import org.keycloak.representations.idm.authorization.UserPolicyRepresentation;

import java.util.Set;

/**
 * org.keycloak.authorization.admin.PolicyService#create(java.lang.String) (for both)
 * org.keycloak.authorization.admin.PermissionService (for permission)
 * <p>
 * <p>
 * see:
 * * https://keycloak.discourse.group/t/how-do-i-create-policies-via-api/15781/4
 */
public class PolicyAdminAdapterTest {
    final PolicyAdminAdapter underTest = new PolicyAdminAdapter(null, null);

    public void testCreatePolicy() {
        RealmModel realm = null;// session.getContext().getRealm();
        UserPolicyRepresentation policy = new UserPolicyRepresentation();
        policy.setType("user");
        policy.setName("abc");
        policy.setDescription("");
        policy.setDecisionStrategy(DecisionStrategy.UNANIMOUS);
        policy.setLogic(Logic.POSITIVE);
        policy.addUser("xxxxx-name");
        underTest.createPolicy(policy);
    }

    public void testCreatePermission() {
        RealmModel realm = null;// session.getContext().getRealm();
        ScopePermissionRepresentation permission = new ScopePermissionRepresentation();
        permission.addResource("6079a169-553b-4e71-b0a6-7cb329f76b6f");
        permission.setPolicies(Set.of("27ac1c9a-57e3-41c8-973b-b5b2849a323e"));
        permission.setScopes(Set.of("view-members", "manage-membership", "manage-members", "view", "manage"));
        permission.setName("all on psi");
        permission.setDescription("bla");
        permission.setResourceType("Groups");
        underTest.createPermission(permission);
    }
}
