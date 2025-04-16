package xx.scicat.keycloakplugin.overridedefaults.permissions;

import org.keycloak.models.GroupModel;
import org.keycloak.services.resources.admin.permissions.GroupPermissionEvaluator;

import java.util.Map;
import java.util.Set;

public class GroupPermissionEvaluatorOverride implements GroupPermissionEvaluator {
    private final GroupPermissionEvaluator baseAuth;

    public GroupPermissionEvaluatorOverride(GroupPermissionEvaluator baseAuth) {
        this.baseAuth = baseAuth;
    }

    @Override
    public boolean canList() {
        return baseAuth.canList();
    }

    @Override
    public void requireList() {
        baseAuth.requireList();
    }

    @Override
    public boolean canManage(GroupModel groupModel) {
        return baseAuth.canManage(groupModel);
    }

    @Override
    public void requireManage(GroupModel groupModel) {
        baseAuth.requireManage(groupModel);
    }

    @Override
    public boolean canView(GroupModel groupModel) {
        return baseAuth.canView(groupModel);
    }

    @Override
    public void requireView(GroupModel groupModel) {
        baseAuth.requireView(groupModel);
    }

    @Override
    public boolean canManage() {
        return baseAuth.canManage();
    }

    @Override
    public void requireManage() {
        baseAuth.requireManage();
    }

    @Override
    public boolean canView() {
        return baseAuth.canView();
    }

    @Override
    public void requireView() {
        baseAuth.requireView();
    }

    @Override
    public boolean getGroupsWithViewPermission(GroupModel groupModel) {
        return baseAuth.getGroupsWithViewPermission(groupModel);
    }

    @Override
    public void requireViewMembers(GroupModel groupModel) {
        baseAuth.requireViewMembers(groupModel);
    }

    @Override
    public boolean canManageMembers(GroupModel groupModel) {
        return baseAuth.canManageMembers(groupModel);
    }

    @Override
    public boolean canManageMembership(GroupModel groupModel) {
        return baseAuth.canManageMembership(groupModel);
    }

    @Override
    public boolean canViewMembers(GroupModel groupModel) {
        return baseAuth.canViewMembers(groupModel);
    }

    @Override
    public void requireManageMembership(GroupModel groupModel) {
        baseAuth.requireManageMembership(groupModel);
    }

    @Override
    public void requireManageMembers(GroupModel groupModel) {
        baseAuth.requireManageMembers(groupModel);
    }

    @Override
    public Map<String, Boolean> getAccess(GroupModel groupModel) {
        return baseAuth.getAccess(groupModel);
    }

    @Override
    public Set<String> getGroupsWithViewPermission() {
        return baseAuth.getGroupsWithViewPermission();
    }
}
