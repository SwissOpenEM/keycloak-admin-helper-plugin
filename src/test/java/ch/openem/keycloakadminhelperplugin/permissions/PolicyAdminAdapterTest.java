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

    //    @POST
    //    @Consumes(MediaType.APPLICATION_JSON)
    //    @Produces(MediaType.APPLICATION_JSON)
    //    @NoCache
    //    @APIResponse(responseCode = "201", description = "Created")
    //    public Response create(String payload) {
    //        if (auth != null) {
    //            this.auth.realm().requireManageAuthorization(resourceServer);
    //        }
    //
    //        AbstractPolicyRepresentation representation = doCreateRepresentation(payload);
    //        Policy policy = create(representation);
    //
    //        representation.setId(policy.getId());
    //
    //        audit(representation, representation.getId(), OperationType.CREATE, authorization.getKeycloakSession());
    //
    //        return Response.status(Response.Status.CREATED).entity(representation).build();
    //    }

    /**
     * <h2>create policy:</h2>
     * <pre>
     * curl 'http://localhost:8024/admin/realms/scicat/clients/25c12e6b-407f-4eb3-af90-b89a70ae77cb/authz/resource-server/policy/user'
     * -X POST
     * -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:137.0) Gecko/20100101 Firefox/137.0'
     * -H 'Accept: application/json, text/plain, * / *' -H 'Accept-Language:en-US,en;q=0.5'
     * -H 'Accept-Encoding:gzip,deflate,br,zstd'
     * -H 'authorization: Bearer 'xx'
     * -H 'content-type:application/json'
     * -H 'Origin:http://localhost:8024'
     * -H 'Connection: keep-alive'
     * -H 'Sec-Fetch-Dest: empty'
     * -H 'Sec-Fetch-Mode: cors'
     * -H 'Sec-Fetch-Site: same-origin'
     * -H 'Priority: u=4'
     * --data-raw '{"type":"user","name":"abc","description":"","decisionStrategy":"UNANIMOUS","logic":"POSITIVE","users":["1e20805e-ed9d-494e-8041-d4e833bcb326"]}'
     * {
     *   "type": "user",
     *   "name": "abc",
     *   "description": "",
     *   "decisionStrategy": "UNANIMOUS",
     *   "logic": "POSITIVE",
     *   "users": [
     *     "1e20805e-ed9d-494e-8041-d4e833bcb326"
     *   ]
     * }
     * </pre>
     */
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


    /**
     * <h2>save permission</h2>
     * <pre>
     * curl 'http://localhost:8024/admin/realms/scicat/clients/25c12e6b-407f-4eb3-af90-b89a70ae77cb/authz/resource-server/permission/scope'
     * -X POST
     * -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:137.0) Gecko/20100101 Firefox/137.0'
     * -H 'Accept: application/json, text/plain, * / *'
     * -H 'Accept-Language: en-US,en;q=0.5'
     * -H 'Accept-Encoding: gzip, deflate, br, zstd'
     * -H 'authorization: Bearer xx'
     * -H 'content-type: application/json'
     * -H 'Origin: http://localhost:8024'
     * -H 'Connection: keep-alive'
     * -H 'Sec-Fetch-Dest: empty'
     * -H 'Sec-Fetch-Mode: cors'
     * -H 'Sec-Fetch-Site: same-origin'
     * -H 'Priority: u=4'
     * --data-raw '{"resources":["6079a169-553b-4e71-b0a6-7cb329f76b6f"],"policies":["27ac1c9a-57e3-41c8-973b-b5b2849a323e"],"scopes":["view-members","manage-membership","manage-members","view","manage"],"name":"all on psi","description":"","resourceType":"Groups"}'
     * {
     *   "resources": [
     *     "6079a169-553b-4e71-b0a6-7cb329f76b6f"
     *   ],
     *   "policies": [
     *     "27ac1c9a-57e3-41c8-973b-b5b2849a323e"
     *   ],
     *   "scopes": [
     *     "view-members",
     *     "manage-membership",
     *     "manage-members",
     *     "view",
     *     "manage"
     *   ],
     *   "name": "all on psi",
     *   "description": "",
     *   "resourceType": "Groups"
     * }
     * </pre>
     */
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
