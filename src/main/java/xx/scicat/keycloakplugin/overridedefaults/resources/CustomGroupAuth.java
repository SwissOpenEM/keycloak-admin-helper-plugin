package xx.scicat.keycloakplugin.overridedefaults.resources;

import jakarta.ws.rs.ForbiddenException;
import org.keycloak.models.GroupModel;
import org.keycloak.models.OrganizationModel;
import org.keycloak.services.resources.admin.permissions.GroupPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.MgmtPermissions;

public class CustomGroupAuth {
    private final GroupPermissionEvaluator baseAuth;

    public CustomGroupAuth(GroupPermissionEvaluator groups) {
        baseAuth=groups;
    }

    public void requireManage(GroupModel group) {
        if (!canManage(group)) {
            throw new ForbiddenException();
        }
    }

    public boolean canManage(GroupModel group) {
        if (baseAuth.canManage(group)) {
            return true;
        }



        if (baseAuth. hasPermission(group, MgmtPermissions.MANAGE_SCOPE))return true;

        return false;
    }


    private boolean hasPermission(String groupId, String... scopes) {
        return baseAuth.hasPermission(groupId, null, scopes);
    }



    private boolean hasFacilityAdminRole(OrganizationModel org, String roleName) {
    /*
    if (!hasOrgRoleInToken(org, roleName)) {
      log.debugf("%s not in token %s", roleName, getToken().getOtherClaims());
      return false;
    }
    */
        OrganizationRoleModel role = org.getRoleByName(roleName);
        boolean has = (role != null && role.hasRole(getUser()));
        log.debugf("%s has role %s? %b", getUser().getId(), roleName, has);
        return has;
    }


}
