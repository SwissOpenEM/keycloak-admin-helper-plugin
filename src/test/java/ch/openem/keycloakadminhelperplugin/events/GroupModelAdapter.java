package ch.openem.keycloakadminhelperplugin.events;

import org.apache.commons.lang3.NotImplementedException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RoleModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GroupModelAdapter implements GroupModel {
    private String name;
    private GroupModel parent;
    private Map<String, List<String>> attribs = new HashMap<>();

    @Override
    public String getId() {
        throw new NotImplementedException();
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public void setName(String s) {
        name = s;
    }

    @Override
    public void setSingleAttribute(String key, String value) {
        attribs.put(key, List.of(value));
    }

    @Override
    public void setAttribute(String key, List<String> list) {
        attribs.put(key, list);
    }

    @Override
    public void removeAttribute(String s) {
        throw new NotImplementedException();
    }

    @Override
    public String getFirstAttribute(String key) {
        List<String> list = attribs.get(key);
        if (list == null) return null;
        return list.get(0);
    }

    @Override
    public Stream<String> getAttributeStream(String s) {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return attribs;
    }

    @Override
    public GroupModel getParent() {
        return parent;
    }

    @Override
    public String getParentId() {
        throw new NotImplementedException();
    }

    @Override
    public Stream<GroupModel> getSubGroupsStream() {
        throw new NotImplementedException();
    }

    @Override
    public void setParent(GroupModel groupModel) {
        parent = groupModel;
    }

    @Override
    public void addChild(GroupModel groupModel) {
        throw new NotImplementedException();
    }

    @Override
    public void removeChild(GroupModel groupModel) {
        throw new NotImplementedException();
    }

    @Override
    public Stream<RoleModel> getRealmRoleMappingsStream() {
        throw new NotImplementedException();
    }

    @Override
    public Stream<RoleModel> getClientRoleMappingsStream(ClientModel clientModel) {
        throw new NotImplementedException();
    }

    @Override
    public boolean hasRole(RoleModel roleModel) {
        throw new NotImplementedException();
    }

    @Override
    public void grantRole(RoleModel roleModel) {
        throw new NotImplementedException();
    }

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        throw new NotImplementedException();
    }

    @Override
    public void deleteRoleMapping(RoleModel roleModel) {
        throw new NotImplementedException();
    }
}
