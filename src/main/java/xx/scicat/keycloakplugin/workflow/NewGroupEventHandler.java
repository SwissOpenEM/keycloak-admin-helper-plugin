package xx.scicat.keycloakplugin.workflow;

import org.jboss.logging.Logger;
import org.keycloak.authorization.model.Policy;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.Logic;
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;
import org.keycloak.representations.idm.authorization.UserPolicyRepresentation;
import xx.scicat.keycloakplugin.permissions.AdminAdapter;
import xx.scicat.keycloakplugin.permissions.PolicyAdminAdapter;

import java.util.Set;

public class NewGroupEventHandler {
    public static final String FACILITY_NAME_ATTR = "facility-name";
    private static final Logger LOG = Logger.getLogger(NewGroupEventHandler.class);
    private final KeycloakSession session;

    public NewGroupEventHandler(KeycloakSession session) {
        this.session = session;
    }

    private static void setAttributeForGroup(GroupModel group, String facilityName) {
        LOG.warnv("  * set {0} attribute to {1}", FACILITY_NAME_ATTR, facilityName);
        group.setSingleAttribute(FACILITY_NAME_ATTR, facilityName);
    }

    private static void makeGroupParent(GroupModel group) {
        LOG.warnv("  * make group top-level");
        group.setParent(null);
    }

    public void processNewInitializerGroupEvent(RealmModel realm, GroupModel group, String facilityName) {
        LOG.warnv("New init facility group in realm {0}: {1} {2} isSubGroup={3}", realm.getName(), group, group.getName(), group.getParent() != null);

        AdminAdapter admin = new AdminAdapter(realm, session);
        PolicyAdminAdapter policyAdmin = PolicyAdminAdapter.create(session, realm);

        // collision detection is indirectly implemented in kaycloak itself
        GroupModel existingGroup = session.groups().getGroupByName(realm, null, facilityName);
        if (existingGroup != null) {
            LOG.warnv("Group with name " + facilityName + " already exists. Doing nothing.");
            return;
        }
        group.setName(facilityName);

        UserModel adminUser = admin.getOrCreateAdminUser(realm, facilityName + "-admin");

        setAttributeForGroup(group, facilityName);

        setPermissionAndPolicyForGroup(policyAdmin, group, facilityName);
    }

    public void processNewGroupEvent(RealmModel realm, GroupModel group, String facilityName, GroupModel topGroup) {
        LOG.warnv("New group in realm {0}: {1} {2} isSubGroup={3}", realm.getName(), group, group.getName(), group.getParent() != null);

        PolicyAdminAdapter policyAdmin = PolicyAdminAdapter.create(session, realm);

//        String newName = findCollisionFreeGroupName(realm, group, facilityName);
//        if (!group.getName().equals(newName)) {
//            LOG.warnv("  * rename group to {0}", newName);
//            group.setName(newName);
//        }
//        makeGroupParent(group);
        setAttributeForGroup(group, facilityName);

        setPermissionAndPolicyForGroup(policyAdmin, group, facilityName);

//        Policy policyToBeEdited = policyStore.findByName(resourceServer, oldPolicyName);
//        AbstractPolicyRepresentation representation = ModelToRepresentation.toRepresentation(policyToBeEdited, authz);
//        representation.setName(newPolicyName);
//        representation.setId(policyToBeEdited.getId());
//
//        RepresentationToModel.toModel(representation, authz, policyToBeEdited);
//
//
//        AdminAuth adminAuth = new AdminAuth(realm, auth.getToken(), auth.getUser(), auth.getClient());
//        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, adminAuth, session, session.getContext().getConnection());
//        session.getTransactionManager().commit();
//        adminEvent.operation(OperationType.UPDATE).resourcePath(authz.getKeycloakSession().getContext().getUri()).representation(representation).success();
    }

    private void setPermissionAndPolicyForGroup(PolicyAdminAdapter policyAdmin, GroupModel group, String facilityName) {
        // set Fine Grained Admin Permissions (https://github.com/keycloak/keycloak/discussions/37133)
        Policy policy = createOrGetPolicy(policyAdmin, facilityName);
        createOrAddToGroupPermission(policyAdmin, group, facilityName, policy);
    }


    private Policy createOrGetPolicy(PolicyAdminAdapter policyAdmin, String facilityName) {
        UserPolicyRepresentation policyRep = new UserPolicyRepresentation();
        policyRep.setType("user");
        policyRep.setName("allow " + facilityName + " admin users policy");
        policyRep.setDescription(facilityName + " groups administration for " + facilityName + " admin users");
        policyRep.setDecisionStrategy(DecisionStrategy.UNANIMOUS);
        policyRep.setLogic(Logic.POSITIVE);
        policyRep.addUser(facilityName + "-admin"); // will result in an empty policy if user doesn't exist
        return policyAdmin.createPolicy(policyRep);
    }


    private void createOrAddToGroupPermission(PolicyAdminAdapter policyAdmin, GroupModel group, String facilityName, Policy policy) {
        final String name = facilityName + " admin for " + facilityName + ", group " + groupPath(group);
        final Set<String> scopes = Set.of("view-members", "manage-membership", "manage-members", "view", "manage");

        // problem: creating: no problem, updating: big api mess. ->!! with this code commented out, permission name must be unique
        // https://stackoverflow.com/questions/79598549/keycloak-plugin-add-group-to-existing-permission
//        final String name = facilityName + " admin for all " + facilityName + " groups";
//            if (policyAdmin.addToExistingPermission_trial2(name, group, scopes)) {
//                LOG.warn("added to already-existing permission");
//                return;
//            }

        ScopePermissionRepresentation permissionRep = new ScopePermissionRepresentation();
        permissionRep.setResourceType("Groups");
        permissionRep.addResource(group.getId());
        permissionRep.setPolicies(Set.of(policy.getId()));
        permissionRep.setScopes(scopes);
        permissionRep.setName(name);
        permissionRep.setDescription("Allow " + facilityName + " admins to change group members and settings of " + facilityName + " groups");
        policyAdmin.createPermission(permissionRep);
    }

    private String groupPath(GroupModel group) {
        StringBuilder result = new StringBuilder(group.getName());
        while (group.getParent() != null) {
            group = group.getParent();
            result.insert(0, group.getName() + "/");
        }
        return result.toString();
    }

    private String findCollisionFreeGroupName(RealmModel realm, GroupModel group, String facilityName) {
        String newNameUnnumbered = group.getName();
        if (!newNameUnnumbered.startsWith(facilityName + "-")) {
            newNameUnnumbered = facilityName + "-" + newNameUnnumbered;
        }
        String newName = newNameUnnumbered;
        for (int i = 1; ; i++) {
            if (noCollision(realm, null, group, newName)
                    && noCollision(realm, group.getParent(), group, newName))
                break;
            newName = newNameUnnumbered + i;
        }
        return newName;
    }

    private boolean noCollision(RealmModel realm, GroupModel checkInParent, GroupModel groupToBeRenamed, String newName) {
        GroupModel foundGroup = session.groups().getGroupByName(realm, checkInParent, newName);
        return foundGroup == null || foundGroup == groupToBeRenamed;
    }
}
