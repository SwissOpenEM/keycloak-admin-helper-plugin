package xx.scicat.keycloakplugin.permissions;

import org.jboss.logging.Logger;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.ResourceWrapper;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;
import org.keycloak.representations.idm.authorization.UserPolicyRepresentation;
import xx.scicat.keycloakplugin.events.ScicatManagementEventListenerProvider;

import java.text.MessageFormat;
import java.util.List;
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
    private final List<Scope> availableScopes;
    private final AuthorizationProvider authorization;
    private final PolicyStore policyStore;

    public static PolicyAdminAdapter create(KeycloakSession session, RealmModel realm) {
        final ClientModel clientModel = realm.getAdminPermissionsClient();
        final AuthorizationProvider authz = requireNonNull(session.getProvider(AuthorizationProvider.class));
        final StoreFactory storeFactory = requireNonNull(authz.getStoreFactory());
        final ResourceServer resourceServer = requireNonNull(storeFactory.getResourceServerStore().findByClient(clientModel));
        final List<Scope> availableScopes = storeFactory.getScopeStore().findByResourceServer(resourceServer);
        return new PolicyAdminAdapter(resourceServer, availableScopes, authz);
    }

    public PolicyAdminAdapter(ResourceServer resourceServer, List<Scope> availableScopes, AuthorizationProvider authorization) {
        this.resourceServer = resourceServer;
        this.availableScopes = availableScopes;
        this.authorization = authorization;
        this.policyStore = authorization.getStoreFactory().getPolicyStore();
    }

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
     *
     * @return
     */
    public Policy createPolicy(UserPolicyRepresentation policyRep) {
//        authorization.policies().user().create(policyRep);

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
     *
     * @return
     */
    public Policy createPermission(ScopePermissionRepresentation permissionRep) {
        return policyStore.create(resourceServer, permissionRep);
    }

    public boolean addToExistingPermission(String permissionName, GroupModel group, Set<String> scopes) {
        Policy permission = getPermissionByName(permissionName);
        if (permission == null) return false;

        permission.getResources().forEach(resource -> {
          LOG.warnv("::{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}", resource, resource.getId(), resource.getName(), resource.getDisplayName(), resource.getType(), resource.getIconUri(), resource.getOwner(), resource.getScopes().stream().map(r -> r.getName()).collect(Collectors.joining()));
        });
        permission.addResource(new ResourceWrapper(
                group.getId(), group.getName(),
                availableScopes.stream().filter(s -> scopes.contains(s.getName())).collect(Collectors.toSet()),
                resourceServer));
//        permission.addResource(new ResourceWrapper(
//                group.getId(), group.getName(),
//                availableScopes.stream().filter(s -> scopes.contains(s.getName())).collect(Collectors.toSet()),
//                resourceServer));
        return true;
    }

    private Policy getPermissionByName(String permissionName) {
        return policyStore.findByName(resourceServer, permissionName);
    }
}

class ScopeWrapper implements Scope {
    private String id;
    private String name;
    private String displayName;
    private String iconUri;
    private ResourceServer resourceServer;


    public ScopeWrapper(String scope) {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getIconUri() {
        return iconUri;
    }

    @Override
    public void setIconUri(String iconUri) {
        this.iconUri = iconUri;
    }

    @Override
    public ResourceServer getResourceServer() {
        return resourceServer;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setResourceServer(ResourceServer resourceServer) {
        this.resourceServer = resourceServer;
    }
}
