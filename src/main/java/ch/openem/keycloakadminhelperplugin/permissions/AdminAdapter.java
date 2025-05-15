package ch.openem.keycloakadminhelperplugin.permissions;

import org.jboss.logging.Logger;
import org.keycloak.models.*;

import static java.util.Objects.requireNonNull;

public class AdminAdapter {
    private static final Logger LOG = Logger.getLogger(AdminAdapter.class);

    private final RealmModel realm;
    private final KeycloakSession session;

    public AdminAdapter(RealmModel realm, KeycloakSession session) {
        this.realm = realm;
        this.session = session;
    }

    public GroupModel getOrCreateGroup(RealmModel realm, GroupModel parentGroup, String groupName) {
        GroupModel templateGroup = session.groups().getGroupByName(realm, parentGroup, groupName);
        if (templateGroup == null) {
            templateGroup = createGroup(realm, parentGroup, groupName);
        }
        return templateGroup;
    }

    public GroupModel createGroup(RealmModel realm, GroupModel parentGroup, String groupName) {
        return session.groups().createGroup(realm, groupName, parentGroup);
    }

    public UserModel getOrCreateAdminUser(RealmModel realm, String userName) {
        UserModel templateUser = session.users().getUserByUsername(realm, userName);
        if (templateUser == null) {
            templateUser = createAdminUser(realm, userName);
        }
        return templateUser;
    }

    public UserModel createAdminUser(RealmModel realm, String userName) {
        UserModel user = session.users().addUser(realm, userName);
        ClientModel realmManagementClient = getRealmManagementClient(realm);
        user.grantRole(requireNonNull(realmManagementClient.getRole(AdminRoles.VIEW_USERS)));
        // user.grantRole(requireNonNull(realmManagementClient.getRole(AdminRoles.QUERY_USERS)));
        // user.grantRole(requireNonNull(realmManagementClient.getRole(AdminRoles.QUERY_GROUPS)));
        user.setEnabled(true);
        return user;
    }

    public void removeGroup(RealmModel realm, GroupModel group) {
        session.groups().removeGroup(realm, group);
    }

    public static ClientModel getRealmManagementClient(RealmModel realm) {
        return realm.getClientByClientId(Constants.REALM_MANAGEMENT_CLIENT_ID);
    }
}