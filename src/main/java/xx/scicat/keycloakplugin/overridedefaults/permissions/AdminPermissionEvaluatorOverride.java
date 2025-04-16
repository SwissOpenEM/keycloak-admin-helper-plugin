package xx.scicat.keycloakplugin.overridedefaults.permissions;

import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.*;

public class AdminPermissionEvaluatorOverride implements AdminPermissionEvaluator {
    private final AdminPermissionEvaluator baseAuth;

    @Override
    public RealmPermissionEvaluator realm() {
        return baseAuth.realm();
    }

    @Override
    public void requireAnyAdminRole() {
baseAuth.requireAnyAdminRole();
    }

    @Override
    public AdminAuth adminAuth() {
        return baseAuth.adminAuth();
    }

    @Override
    public RolePermissionEvaluator roles() {
        return baseAuth.roles();
    }

    @Override
    public UserPermissionEvaluator users() {
        return baseAuth.users();
    }

    @Override
    public ClientPermissionEvaluator clients() {
        return baseAuth.clients();
    }

    @Override
    public GroupPermissionEvaluator groups() {
        return new GroupPermissionEvaluatorOverride( baseAuth.groups());
    }
}
