package xx.scicat.keycloakplugin.permissions;

import org.jboss.logging.Logger;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.*;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.ResourceStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;
import org.keycloak.representations.idm.authorization.UserPolicyRepresentation;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
        final ClientModel clientModel = requireNonNull(realm.getAdminPermissionsClient(), "AdminPermissionsClient is null. Forgot to enable 'Admin Permissions' in realm?");
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

    /**
     * doesn't work
     */
    @Deprecated
    public boolean addToExistingPermission_doesntworktoo(String permissionName, GroupModel group, Set<String> scopes) throws IOException {
        // https://chatgpt.com/c/6811ef46-ac2c-8010-b103-24897f19dcf3
        Policy policy = getPermissionByName(permissionName);
        if (policy == null) return false;

        Map<String, String> policyConfig = policy.getConfig();
        policyConfig.forEach((s, s2) -> LOG.warn("  polconfig " + s + ": " + s2));
        // cave: there's no "config" entry in this map!

        ScopePermissionRepresentation permissionRep = JsonSerialization.readValue(
                policyConfig.get("config"),
                ScopePermissionRepresentation.class
        );
        permissionRep.addResource(group.getId());
        policyConfig.put("config", JsonSerialization.writeValueAsString(permissionRep));
        return true;
    }

    /**
     * doesn't work
     */
    @Deprecated
    public boolean addToExistingPermission_doesntwork(String permissionName, GroupModel group, Set<String> scopes) {
        Policy permission = getPermissionByName(permissionName);
        if (permission == null) return false;

        LOG.warn("permission: " + permission + " ; type=" + permission.getClass());
        permission.getResources().forEach(resource -> {
            LOG.warnv("::{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}", resource, resource.getId(), resource.getName(), resource.getDisplayName(), resource.getType(), resource.getIconUri(), resource.getOwner(), resource.getScopes().stream().map(r -> r.getName()).collect(Collectors.joining()));
        });
        // instead of ResourceWrapper, we probably should work with an existing Resource, fetched via a function I don't know
        permission.addResource(new ResourceWrapper(
                group.getId(), group.getName(),
                availableScopes.stream().filter(s -> scopes.contains(s.getName())).collect(Collectors.toSet()),
                resourceServer));
        return true;
    }

    /**
     * doesn't work
     */
    @Deprecated
    public boolean addToExistingPermission_trial(String permissionName, GroupModel group, Set<String> scopes) {
        // https://gemini.google.com/app/3c3b0fcffcb768e7

        Policy permission = getPermissionByName(permissionName);
        if (permission == null) return false;

        Set<String> resourceIds = permission.getResources().stream()
                .map(org.keycloak.authorization.model.Resource::getId)
                .collect(Collectors.toSet());

        // Add the new group ID to the set of resource IDs
        resourceIds.add(group.getId());

        // Create a new ScopePermissionRepresentation for the update
        ScopePermissionRepresentation updatedPermissionRep = new ScopePermissionRepresentation();
        updatedPermissionRep.setName(permission.getName());
        updatedPermissionRep.setDescription(permission.getDescription());
        updatedPermissionRep.setPolicies(permission.getAssociatedPolicies().stream().map(Policy::getId).collect(Collectors.toSet()));
        updatedPermissionRep.setScopes(permission.getScopes().stream().map(org.keycloak.authorization.model.Scope::getName).collect(Collectors.toSet()));
        updatedPermissionRep.setResourceType("Groups"); // Important: Set the correct resource type
        updatedPermissionRep.setResources(resourceIds); // Set the updated set of resource IDs

        // Update the permission in the policy store
        // TODO genini cited policyStore.update, but the method doesn't exist
//        policyStore.update(resourceServer, permission.getId(), updatedPermissionRep);

        System.out.println("Successfully added group '" + group.getName() + "' to permission '" + permissionName + "'.");

        return true;
    }

    /**
     * doesn't work
     */
    @Deprecated
    public boolean addToExistingPermission_trial2(String permissionName, GroupModel group, Set<String> scopes) {
        // https://gemini.google.com/app/3c3b0fcffcb768e7

        Policy permission = getPermissionByName(permissionName);
        if (permission == null) return false;

        LOG.warn("permission: " + permission + " ; type=" + permission.getClass());
        permission.getResources().forEach(resource -> {
            LOG.warnv("::{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}", resource, resource.getId(), resource.getName(), resource.getDisplayName(), resource.getType(), resource.getIconUri(), resource.getOwner(), resource.getScopes().stream().map(r -> r.getName()).collect(Collectors.joining()));
        });
        // instead of ResourceWrapper, we probably should work with an existing Resource, fetched via a function I don't know

        final StoreFactory storeFactory = requireNonNull(authorization.getStoreFactory());
        final ResourceStore resourceStore = storeFactory.getResourceStore();
        // the following command throws NPE. groupid cannot be found. group != resourceid!!!
        Resource groupResource = requireNonNull(resourceStore.findById(resourceServer, group.getId()));

        permission.addResource(groupResource);

        return true;
    }

    private Policy getPermissionByName(String permissionName) {
        return policyStore.findByName(resourceServer, permissionName);
    }
}