package ch.openem.keycloakadminhelperplugin.permissions;

import org.jboss.logging.Logger;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;
import org.keycloak.representations.idm.authorization.UserPolicyRepresentation;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * org.keycloak.authorization.admin.PolicyService#create(java.lang.String) (for both)
 * org.keycloak.authorization.admin.PermissionService (for permission)
 * <p>
 * <p>
 * see:
 * * https://keycloak.discourse.group/t/how-do-i-create-policies-via-api/15781/4
 */
public class PolicyAdminAdapter {
    private static final Logger LOG = Logger.getLogger(PolicyAdminAdapter.class);

    private final ResourceServer resourceServer;
    private final AuthorizationProvider authorization;
    private final PolicyStore policyStore;

    public PolicyAdminAdapter(ResourceServer resourceServer, AuthorizationProvider authorization) {
        this.resourceServer = resourceServer;
        this.authorization = authorization;
        this.policyStore = authorization.getStoreFactory().getPolicyStore();
    }

    public static PolicyAdminAdapter create(KeycloakSession session, RealmModel realm) {
        final ClientModel clientModel = requireNonNull(realm.getAdminPermissionsClient(), "AdminPermissionsClient is null. Forgot to enable 'Admin Permissions' in realm?");
        final AuthorizationProvider authz = requireNonNull(session.getProvider(AuthorizationProvider.class));
        final StoreFactory storeFactory = requireNonNull(authz.getStoreFactory());
        final ResourceServer resourceServer = requireNonNull(storeFactory.getResourceServerStore().findByClient(clientModel));
        return new PolicyAdminAdapter(resourceServer, authz);
    }

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
    public Policy createPolicy(UserPolicyRepresentation policyRep) {
        PolicyStore policyStore = authorization.getStoreFactory().getPolicyStore();
        Policy existing = policyStore.findByName(resourceServer, policyRep.getName());
        if (existing != null) return existing;

        return policyStore.create(resourceServer, policyRep);
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
    public Policy createPermission(ScopePermissionRepresentation permissionRep) {
        return policyStore.create(resourceServer, permissionRep);
    }

    /**
     * source: from a question, not an answer in https://github.com/keycloak/keycloak/discussions/26869
     */
    public boolean addGroupToExistingPermission(String permissionName, GroupModel group, Set<String> scopes) {
        Policy permissionToBeEdited = policyStore.findByName(resourceServer, permissionName);
        if (permissionToBeEdited == null) return false;

        PolicyRepresentation representation = ModelToRepresentation.toRepresentation(permissionToBeEdited, authorization);
        /*
        please note: most fields are null. If you want to change a complex type, you probably need to pre-populate it.
        only non-null fields in representation will overwrite the existing permission
         */
        representation.setResources(permissionToBeEdited.getResources().stream().map(Resource::getId).collect(Collectors.toSet()));
        LOG.warn("num res before: " + representation.getResources().size());
        representation.addResource(group.getId());
        LOG.warn("num res after: " + representation.getResources().size());
        // representation.setId(permissionToBeEdited.getId());
        RepresentationToModel.toModel(representation, authorization, permissionToBeEdited);

        // try enabling some of the following lines if you have problems:
        // AdminAuth adminAuth = new AdminAuth(realm, auth.getToken(), auth.getUser(), auth.getClient());
        // AdminEventBuilder adminEvent = new AdminEventBuilder(realm, adminAuth, session, session.getContext().getConnection());
        // session.getTransactionManager().commit();
        // adminEvent.operation(OperationType.UPDATE).resourcePath(authz.getKeycloakSession().getContext().getUri()).representation(representation).success();

        return true;
    }
}