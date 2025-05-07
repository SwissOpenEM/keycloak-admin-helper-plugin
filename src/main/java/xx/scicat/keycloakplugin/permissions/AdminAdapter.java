package xx.scicat.keycloakplugin.permissions;

import org.jboss.logging.Logger;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

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
        // TODO fix the following:
        LOG.warn("Please manually add role view-users to new user " + userName);
        //        user.grantRole(requireNonNull(KeycloakModelUtils.getRoleFromString(realm,AdminRoles.VIEW_USERS)));
        //        user.grantRole(requireNonNull(realm.getRole(AdminRoles.VIEW_USERS)));
        user.setEnabled(true);
        return user;
    }


    public void removeGroup(RealmModel realm, GroupModel group) {
        session.groups().removeGroup(realm, group);
    }
}