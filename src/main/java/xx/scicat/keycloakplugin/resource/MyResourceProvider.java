package xx.scicat.keycloakplugin.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.logging.Logger;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.services.resource.RealmResourceProvider;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
//@RequiredArgsConstructor
//@Path("/realms/{realm}/" + MyResourceProviderFactory.PROVIDER_ID)
public class MyResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;


    public MyResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {
    }

    private static final Logger LOG = Logger.getLogger(MyResourceProvider.class);


    /**
     * without auth!
     */
    @GET
    @Path("hello")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Public hello endpoint",
            description = "This endpoint returns hello and the name of the requested realm."
    )
    @APIResponse(
            responseCode = "200",
            description = "",
            content = {@Content(
                    schema = @Schema(
                            implementation = Response.class,
                            type = SchemaType.OBJECT
                    )
            )}
    )
    public Response helloAnonymous() {
        return Response.ok(Map.of("hello", session.getContext().getRealm().getName())).build();
    }

    @GET
    @Path("hello-auth")
    @Produces(MediaType.APPLICATION_JSON)
    public Response helloAuthenticated() {
        AuthResult auth = checkAuth();

//		session.users().
        LOG.warn("Realm search");
        session.realms().getRealmsStream().forEach(r -> {
            LOG.warn("  RE " + r.getName() + " " + r.getDisplayName());

            session.users().searchForUserStream(r, Map.of()).forEach(u -> {
                LOG.warn("    US " + u.getEmail() + " " + u.getId() + " " + u.getUsername() + " " + u.getFirstName() + " " + u.getLastName());
            });
            try {
                String name = "dummyuser" + new Random().nextInt();
                UserModel u = session.users().addUser(r, name);
                u.setEmail(name + "@trash.net");
                u.setEnabled(true);
            } catch (Exception e) {
                LOG.warn("cannot add user in realm " + r, e);
            }

        });
//		session.users().searchForUserStream()

        return Response.ok(Map.of("hello", auth.getUser().getUsername())).build();
    }

    @GET
    @Path("creategroup")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGroup() {
        AuthResult auth = checkAuth2();

//		session.users().
        LOG.warn("CREATE GROUP");
        String FACILITY_NAME = "facility_name";
        String adminFacility = auth.getUser().getFirstAttribute(FACILITY_NAME);
        RealmModel realm = session.getContext().getRealm();
        if (adminFacility == null)
            throw new ForbiddenException("Admin doesn't have a facility set");


        String name = adminFacility + "_newunnamedgroup_" + new Random().nextLong(100000000L);
        GroupModel g = session.groups().createGroup(realm, name);
        g.setAttribute(FACILITY_NAME, List.of(adminFacility));
//        g
//        u.setEmail(name + "@trash.net");
//        u.setEnabled(true);
//
//        session.realms().getRealmsStream().forEach(r -> {
//            LOG.warn("  RE " + r.getName() + " " + r.getDisplayName());
//
//            session.users().searchForUserStream(r, Map.of()).forEach(u -> {
//                LOG.warn("    US " + u.getEmail() + " " + u.getId() + " " + u.getUsername() + " " + u.getFirstName() + " " + u.getLastName());
//            });
//            try {
//                String name = "dummyuser" + new Random().nextInt();
//                UserModel u = session.users().addUser(r, name);
//                u.setEmail(name + "@trash.net");
//                u.setEnabled(true);
//            } catch (Exception e) {
//                LOG.warn("cannot add user in realm " + r, e);
//            }
//
//        });
//		session.users().searchForUserStream()

        return Response.ok(Map.of(
                "hello", auth.getUser().getUsername(),
                "status", "Group " + name + " added")).build();
    }

    @GET
    @Path("hello-add")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser() {
        AuthResult auth = checkAuth();

        RealmModel realm = session.getContext().getRealm();

        LOG.warn("r=" + realm);


        String name = "dummyuser" + new Random().nextInt();
        UserModel u = session.users().addUser(realm, name);
        u.setEmail(name + "@trash.net");
        u.setEnabled(true);


        return Response.ok(Map.of("realm", realm.getName(), "hello", auth.getUser().getUsername())).build();
    }

    @GET
    @Path("hello-getusers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getusers() {
        AuthResult auth = checkAuth();

        RealmModel realm = session.getContext().getRealm();

        List<Map<String, String>> userslist = session.users().searchForUserStream(realm, Map.of()).peek(u -> {
            LOG.warn("    US " + u.getEmail() + " " + u.getId() + " " + u.getUsername() + " " + u.getFirstName() + " " + u.getLastName());
        }).map(u -> Map.of(
                "mail", nvl(u.getEmail()),
                "id", nvl(u.getId()),
                "username", nvl(u.getUsername()),
                "firstname", nvl(u.getFirstName()),
                "lastname", nvl(u.getLastName()))).toList();


        return Response.ok(Map.of(
                "realm", realm.getName(),
                "hello", auth.getUser().getUsername(),
                "users", userslist)).build();
    }

    private String nvl(String value) {
        return value != null ? value : "-";
    }

    private AuthResult checkAuth() {
        AuthResult auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
        if (auth == null) {
            LOG.warn("NotAuthorizedException(\"Bearer\")");
            throw new NotAuthorizedException("Bearer");
            // other proposal:   if( auth.getToken().getIssuedFor() == null) {
        } else if (auth.getToken().getIssuedFor() == null || !auth.getToken().getIssuedFor().equals("admin-cli")) {
            LOG.warn("ForbiddenException(\"Token is not properly issued for admin-cli\")");
            throw new ForbiddenException("Token is not properly issued for admin-cli");
        }
        return auth;
    }

    private AuthResult checkAuth2() {
        AuthResult auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
        if (auth == null) {
            LOG.warn("NotAuthorizedException(\"Bearer\")");
            throw new NotAuthorizedException("Bearer");
            // other proposal:   if( auth.getToken().getIssuedFor() == null) {
        } else if (auth.getToken().getIssuedFor() == null || !auth.getToken().getIssuedFor().equals("admin-cli")) {
            LOG.warn("ForbiddenException(\"Token is not properly issued for admin-cli\")" + auth.getToken().getIssuedFor());
            throw new ForbiddenException("Token is not properly issued for admin-cli");
        }
        return auth;
    }

    private static class SearchRealmModelAdapter implements RealmModel {
        @Override
        public String getId() {
            return null;
        }

        @Override
        public RoleModel getRole(String s) {
            return null;
        }

        @Override
        public RoleModel addRole(String s) {
            return null;
        }

        @Override
        public RoleModel addRole(String s, String s1) {
            return null;
        }

        @Override
        public boolean removeRole(RoleModel roleModel) {
            return false;
        }

        @Override
        public Stream<RoleModel> getRolesStream() {
            return Stream.empty();
        }

        @Override
        public Stream<RoleModel> getRolesStream(Integer integer, Integer integer1) {
            return Stream.empty();
        }

        @Override
        public Stream<RoleModel> searchForRolesStream(String s, Integer integer, Integer integer1) {
            return Stream.empty();
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void setName(String s) {

        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public void setDisplayName(String s) {

        }

        @Override
        public String getDisplayNameHtml() {
            return null;
        }

        @Override
        public void setDisplayNameHtml(String s) {

        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void setEnabled(boolean b) {

        }

        @Override
        public SslRequired getSslRequired() {
            return null;
        }

        @Override
        public void setSslRequired(SslRequired sslRequired) {

        }

        @Override
        public boolean isRegistrationAllowed() {
            return false;
        }

        @Override
        public void setRegistrationAllowed(boolean b) {

        }

        @Override
        public boolean isRegistrationEmailAsUsername() {
            return false;
        }

        @Override
        public void setRegistrationEmailAsUsername(boolean b) {

        }

        @Override
        public boolean isRememberMe() {
            return false;
        }

        @Override
        public void setRememberMe(boolean b) {

        }

        @Override
        public boolean isEditUsernameAllowed() {
            return false;
        }

        @Override
        public void setEditUsernameAllowed(boolean b) {

        }

        @Override
        public boolean isUserManagedAccessAllowed() {
            return false;
        }

        @Override
        public void setUserManagedAccessAllowed(boolean b) {

        }

        @Override
        public boolean isOrganizationsEnabled() {
            return false;
        }

        @Override
        public void setOrganizationsEnabled(boolean b) {

        }

        @Override
        public boolean isAdminPermissionsEnabled() {
            return false;
        }

        @Override
        public void setAdminPermissionsEnabled(boolean b) {

        }

        @Override
        public boolean isVerifiableCredentialsEnabled() {
            return false;
        }

        @Override
        public void setVerifiableCredentialsEnabled(boolean b) {

        }

        @Override
        public void setAttribute(String s, String s1) {

        }

        @Override
        public void removeAttribute(String s) {

        }

        @Override
        public String getAttribute(String s) {
            return null;
        }

        @Override
        public Map<String, String> getAttributes() {
            return Map.of();
        }

        @Override
        public boolean isBruteForceProtected() {
            return false;
        }

        @Override
        public void setBruteForceProtected(boolean b) {

        }

        @Override
        public boolean isPermanentLockout() {
            return false;
        }

        @Override
        public void setPermanentLockout(boolean b) {

        }

        @Override
        public int getMaxTemporaryLockouts() {
            return 0;
        }

        @Override
        public void setMaxTemporaryLockouts(int i) {

        }

        @Override
        public RealmRepresentation.BruteForceStrategy getBruteForceStrategy() {
            return null;
        }

        @Override
        public void setBruteForceStrategy(RealmRepresentation.BruteForceStrategy bruteForceStrategy) {

        }

        @Override
        public int getMaxFailureWaitSeconds() {
            return 0;
        }

        @Override
        public void setMaxFailureWaitSeconds(int i) {

        }

        @Override
        public int getWaitIncrementSeconds() {
            return 0;
        }

        @Override
        public void setWaitIncrementSeconds(int i) {

        }

        @Override
        public int getMinimumQuickLoginWaitSeconds() {
            return 0;
        }

        @Override
        public void setMinimumQuickLoginWaitSeconds(int i) {

        }

        @Override
        public long getQuickLoginCheckMilliSeconds() {
            return 0;
        }

        @Override
        public void setQuickLoginCheckMilliSeconds(long l) {

        }

        @Override
        public int getMaxDeltaTimeSeconds() {
            return 0;
        }

        @Override
        public void setMaxDeltaTimeSeconds(int i) {

        }

        @Override
        public int getFailureFactor() {
            return 0;
        }

        @Override
        public void setFailureFactor(int i) {

        }

        @Override
        public boolean isVerifyEmail() {
            return false;
        }

        @Override
        public void setVerifyEmail(boolean b) {

        }

        @Override
        public boolean isLoginWithEmailAllowed() {
            return false;
        }

        @Override
        public void setLoginWithEmailAllowed(boolean b) {

        }

        @Override
        public boolean isDuplicateEmailsAllowed() {
            return false;
        }

        @Override
        public void setDuplicateEmailsAllowed(boolean b) {

        }

        @Override
        public boolean isResetPasswordAllowed() {
            return false;
        }

        @Override
        public void setResetPasswordAllowed(boolean b) {

        }

        @Override
        public String getDefaultSignatureAlgorithm() {
            return null;
        }

        @Override
        public void setDefaultSignatureAlgorithm(String s) {

        }

        @Override
        public boolean isRevokeRefreshToken() {
            return false;
        }

        @Override
        public void setRevokeRefreshToken(boolean b) {

        }

        @Override
        public int getRefreshTokenMaxReuse() {
            return 0;
        }

        @Override
        public void setRefreshTokenMaxReuse(int i) {

        }

        @Override
        public int getSsoSessionIdleTimeout() {
            return 0;
        }

        @Override
        public void setSsoSessionIdleTimeout(int i) {

        }

        @Override
        public int getSsoSessionMaxLifespan() {
            return 0;
        }

        @Override
        public void setSsoSessionMaxLifespan(int i) {

        }

        @Override
        public int getSsoSessionIdleTimeoutRememberMe() {
            return 0;
        }

        @Override
        public void setSsoSessionIdleTimeoutRememberMe(int i) {

        }

        @Override
        public int getSsoSessionMaxLifespanRememberMe() {
            return 0;
        }

        @Override
        public void setSsoSessionMaxLifespanRememberMe(int i) {

        }

        @Override
        public int getOfflineSessionIdleTimeout() {
            return 0;
        }

        @Override
        public void setOfflineSessionIdleTimeout(int i) {

        }

        @Override
        public int getAccessTokenLifespan() {
            return 0;
        }

        @Override
        public boolean isOfflineSessionMaxLifespanEnabled() {
            return false;
        }

        @Override
        public void setOfflineSessionMaxLifespanEnabled(boolean b) {

        }

        @Override
        public int getOfflineSessionMaxLifespan() {
            return 0;
        }

        @Override
        public void setOfflineSessionMaxLifespan(int i) {

        }

        @Override
        public int getClientSessionIdleTimeout() {
            return 0;
        }

        @Override
        public void setClientSessionIdleTimeout(int i) {

        }

        @Override
        public int getClientSessionMaxLifespan() {
            return 0;
        }

        @Override
        public void setClientSessionMaxLifespan(int i) {

        }

        @Override
        public int getClientOfflineSessionIdleTimeout() {
            return 0;
        }

        @Override
        public void setClientOfflineSessionIdleTimeout(int i) {

        }

        @Override
        public int getClientOfflineSessionMaxLifespan() {
            return 0;
        }

        @Override
        public void setClientOfflineSessionMaxLifespan(int i) {

        }

        @Override
        public void setAccessTokenLifespan(int i) {

        }

        @Override
        public int getAccessTokenLifespanForImplicitFlow() {
            return 0;
        }

        @Override
        public void setAccessTokenLifespanForImplicitFlow(int i) {

        }

        @Override
        public int getAccessCodeLifespan() {
            return 0;
        }

        @Override
        public void setAccessCodeLifespan(int i) {

        }

        @Override
        public int getAccessCodeLifespanUserAction() {
            return 0;
        }

        @Override
        public void setAccessCodeLifespanUserAction(int i) {

        }

        @Override
        public OAuth2DeviceConfig getOAuth2DeviceConfig() {
            return null;
        }

        @Override
        public CibaConfig getCibaPolicy() {
            return null;
        }

        @Override
        public ParConfig getParPolicy() {
            return null;
        }

        @Override
        public Map<String, Integer> getUserActionTokenLifespans() {
            return Map.of();
        }

        @Override
        public int getAccessCodeLifespanLogin() {
            return 0;
        }

        @Override
        public void setAccessCodeLifespanLogin(int i) {

        }

        @Override
        public int getActionTokenGeneratedByAdminLifespan() {
            return 0;
        }

        @Override
        public void setActionTokenGeneratedByAdminLifespan(int i) {

        }

        @Override
        public int getActionTokenGeneratedByUserLifespan() {
            return 0;
        }

        @Override
        public void setActionTokenGeneratedByUserLifespan(int i) {

        }

        @Override
        public int getActionTokenGeneratedByUserLifespan(String s) {
            return 0;
        }

        @Override
        public void setActionTokenGeneratedByUserLifespan(String s, Integer integer) {

        }

        @Override
        public Stream<RequiredCredentialModel> getRequiredCredentialsStream() {
            return Stream.empty();
        }

        @Override
        public void addRequiredCredential(String s) {

        }

        @Override
        public PasswordPolicy getPasswordPolicy() {
            return null;
        }

        @Override
        public void setPasswordPolicy(PasswordPolicy passwordPolicy) {

        }

        @Override
        public OTPPolicy getOTPPolicy() {
            return null;
        }

        @Override
        public void setOTPPolicy(OTPPolicy otpPolicy) {

        }

        @Override
        public WebAuthnPolicy getWebAuthnPolicy() {
            return null;
        }

        @Override
        public void setWebAuthnPolicy(WebAuthnPolicy webAuthnPolicy) {

        }

        @Override
        public WebAuthnPolicy getWebAuthnPolicyPasswordless() {
            return null;
        }

        @Override
        public void setWebAuthnPolicyPasswordless(WebAuthnPolicy webAuthnPolicy) {

        }

        @Override
        public RoleModel getRoleById(String s) {
            return null;
        }

        @Override
        public Stream<GroupModel> getDefaultGroupsStream() {
            return Stream.empty();
        }

        @Override
        public void addDefaultGroup(GroupModel groupModel) {

        }

        @Override
        public void removeDefaultGroup(GroupModel groupModel) {

        }

        @Override
        public Stream<ClientModel> getClientsStream() {
            return Stream.empty();
        }

        @Override
        public Stream<ClientModel> getClientsStream(Integer integer, Integer integer1) {
            return Stream.empty();
        }

        @Override
        public Long getClientsCount() {
            return 0L;
        }

        @Override
        public Stream<ClientModel> getAlwaysDisplayInConsoleClientsStream() {
            return Stream.empty();
        }

        @Override
        public ClientModel addClient(String s) {
            return null;
        }

        @Override
        public ClientModel addClient(String s, String s1) {
            return null;
        }

        @Override
        public boolean removeClient(String s) {
            return false;
        }

        @Override
        public ClientModel getClientById(String s) {
            return null;
        }

        @Override
        public ClientModel getClientByClientId(String s) {
            return null;
        }

        @Override
        public Stream<ClientModel> searchClientByClientIdStream(String s, Integer integer, Integer integer1) {
            return Stream.empty();
        }

        @Override
        public Stream<ClientModel> searchClientByAttributes(Map<String, String> map, Integer integer, Integer integer1) {
            return Stream.empty();
        }

        @Override
        public Stream<ClientModel> searchClientByAuthenticationFlowBindingOverrides(Map<String, String> map, Integer integer, Integer integer1) {
            return Stream.empty();
        }

        @Override
        public void updateRequiredCredentials(Set<String> set) {

        }

        @Override
        public Map<String, String> getBrowserSecurityHeaders() {
            return Map.of();
        }

        @Override
        public void setBrowserSecurityHeaders(Map<String, String> map) {

        }

        @Override
        public Map<String, String> getSmtpConfig() {
            return Map.of();
        }

        @Override
        public void setSmtpConfig(Map<String, String> map) {

        }

        @Override
        public AuthenticationFlowModel getBrowserFlow() {
            return null;
        }

        @Override
        public void setBrowserFlow(AuthenticationFlowModel authenticationFlowModel) {

        }

        @Override
        public AuthenticationFlowModel getRegistrationFlow() {
            return null;
        }

        @Override
        public void setRegistrationFlow(AuthenticationFlowModel authenticationFlowModel) {

        }

        @Override
        public AuthenticationFlowModel getDirectGrantFlow() {
            return null;
        }

        @Override
        public void setDirectGrantFlow(AuthenticationFlowModel authenticationFlowModel) {

        }

        @Override
        public AuthenticationFlowModel getResetCredentialsFlow() {
            return null;
        }

        @Override
        public void setResetCredentialsFlow(AuthenticationFlowModel authenticationFlowModel) {

        }

        @Override
        public AuthenticationFlowModel getClientAuthenticationFlow() {
            return null;
        }

        @Override
        public void setClientAuthenticationFlow(AuthenticationFlowModel authenticationFlowModel) {

        }

        @Override
        public AuthenticationFlowModel getDockerAuthenticationFlow() {
            return null;
        }

        @Override
        public void setDockerAuthenticationFlow(AuthenticationFlowModel authenticationFlowModel) {

        }

        @Override
        public AuthenticationFlowModel getFirstBrokerLoginFlow() {
            return null;
        }

        @Override
        public void setFirstBrokerLoginFlow(AuthenticationFlowModel authenticationFlowModel) {

        }

        @Override
        public Stream<AuthenticationFlowModel> getAuthenticationFlowsStream() {
            return Stream.empty();
        }

        @Override
        public AuthenticationFlowModel getFlowByAlias(String s) {
            return null;
        }

        @Override
        public AuthenticationFlowModel addAuthenticationFlow(AuthenticationFlowModel authenticationFlowModel) {
            return null;
        }

        @Override
        public AuthenticationFlowModel getAuthenticationFlowById(String s) {
            return null;
        }

        @Override
        public void removeAuthenticationFlow(AuthenticationFlowModel authenticationFlowModel) {

        }

        @Override
        public void updateAuthenticationFlow(AuthenticationFlowModel authenticationFlowModel) {

        }

        @Override
        public Stream<AuthenticationExecutionModel> getAuthenticationExecutionsStream(String s) {
            return Stream.empty();
        }

        @Override
        public AuthenticationExecutionModel getAuthenticationExecutionById(String s) {
            return null;
        }

        @Override
        public AuthenticationExecutionModel getAuthenticationExecutionByFlowId(String s) {
            return null;
        }

        @Override
        public AuthenticationExecutionModel addAuthenticatorExecution(AuthenticationExecutionModel authenticationExecutionModel) {
            return null;
        }

        @Override
        public void updateAuthenticatorExecution(AuthenticationExecutionModel authenticationExecutionModel) {

        }

        @Override
        public void removeAuthenticatorExecution(AuthenticationExecutionModel authenticationExecutionModel) {

        }

        @Override
        public Stream<AuthenticatorConfigModel> getAuthenticatorConfigsStream() {
            return Stream.empty();
        }

        @Override
        public AuthenticatorConfigModel addAuthenticatorConfig(AuthenticatorConfigModel authenticatorConfigModel) {
            return null;
        }

        @Override
        public void updateAuthenticatorConfig(AuthenticatorConfigModel authenticatorConfigModel) {

        }

        @Override
        public void removeAuthenticatorConfig(AuthenticatorConfigModel authenticatorConfigModel) {

        }

        @Override
        public AuthenticatorConfigModel getAuthenticatorConfigById(String s) {
            return null;
        }

        @Override
        public AuthenticatorConfigModel getAuthenticatorConfigByAlias(String s) {
            return null;
        }

        @Override
        public RequiredActionConfigModel getRequiredActionConfigById(String s) {
            return null;
        }

        @Override
        public RequiredActionConfigModel getRequiredActionConfigByAlias(String s) {
            return null;
        }

        @Override
        public void removeRequiredActionProviderConfig(RequiredActionConfigModel requiredActionConfigModel) {

        }

        @Override
        public void updateRequiredActionConfig(RequiredActionConfigModel requiredActionConfigModel) {

        }

        @Override
        public Stream<RequiredActionConfigModel> getRequiredActionConfigsStream() {
            return Stream.empty();
        }

        @Override
        public Stream<RequiredActionProviderModel> getRequiredActionProvidersStream() {
            return Stream.empty();
        }

        @Override
        public RequiredActionProviderModel addRequiredActionProvider(RequiredActionProviderModel requiredActionProviderModel) {
            return null;
        }

        @Override
        public void updateRequiredActionProvider(RequiredActionProviderModel requiredActionProviderModel) {

        }

        @Override
        public void removeRequiredActionProvider(RequiredActionProviderModel requiredActionProviderModel) {

        }

        @Override
        public RequiredActionProviderModel getRequiredActionProviderById(String s) {
            return null;
        }

        @Override
        public RequiredActionProviderModel getRequiredActionProviderByAlias(String s) {
            return null;
        }

        @Override
        public Stream<IdentityProviderModel> getIdentityProvidersStream() {
            return Stream.empty();
        }

        @Override
        public IdentityProviderModel getIdentityProviderByAlias(String s) {
            return null;
        }

        @Override
        public void addIdentityProvider(IdentityProviderModel identityProviderModel) {

        }

        @Override
        public void removeIdentityProviderByAlias(String s) {

        }

        @Override
        public void updateIdentityProvider(IdentityProviderModel identityProviderModel) {

        }

        @Override
        public Stream<IdentityProviderMapperModel> getIdentityProviderMappersStream() {
            return Stream.empty();
        }

        @Override
        public Stream<IdentityProviderMapperModel> getIdentityProviderMappersByAliasStream(String s) {
            return Stream.empty();
        }

        @Override
        public IdentityProviderMapperModel addIdentityProviderMapper(IdentityProviderMapperModel identityProviderMapperModel) {
            return null;
        }

        @Override
        public void removeIdentityProviderMapper(IdentityProviderMapperModel identityProviderMapperModel) {

        }

        @Override
        public void updateIdentityProviderMapper(IdentityProviderMapperModel identityProviderMapperModel) {

        }

        @Override
        public IdentityProviderMapperModel getIdentityProviderMapperById(String s) {
            return null;
        }

        @Override
        public IdentityProviderMapperModel getIdentityProviderMapperByName(String s, String s1) {
            return null;
        }

        @Override
        public ComponentModel addComponentModel(ComponentModel componentModel) {
            return null;
        }

        @Override
        public ComponentModel importComponentModel(ComponentModel componentModel) {
            return null;
        }

        @Override
        public void updateComponent(ComponentModel componentModel) {

        }

        @Override
        public void removeComponent(ComponentModel componentModel) {

        }

        @Override
        public void removeComponents(String s) {

        }

        @Override
        public Stream<ComponentModel> getComponentsStream(String s, String s1) {
            return Stream.empty();
        }

        @Override
        public Stream<ComponentModel> getComponentsStream(String s) {
            return Stream.empty();
        }

        @Override
        public Stream<ComponentModel> getComponentsStream() {
            return Stream.empty();
        }

        @Override
        public ComponentModel getComponent(String s) {
            return null;
        }

        @Override
        public String getLoginTheme() {
            return null;
        }

        @Override
        public void setLoginTheme(String s) {

        }

        @Override
        public String getAccountTheme() {
            return null;
        }

        @Override
        public void setAccountTheme(String s) {

        }

        @Override
        public String getAdminTheme() {
            return null;
        }

        @Override
        public void setAdminTheme(String s) {

        }

        @Override
        public String getEmailTheme() {
            return null;
        }

        @Override
        public void setEmailTheme(String s) {

        }

        @Override
        public int getNotBefore() {
            return 0;
        }

        @Override
        public void setNotBefore(int i) {

        }

        @Override
        public boolean isEventsEnabled() {
            return false;
        }

        @Override
        public void setEventsEnabled(boolean b) {

        }

        @Override
        public long getEventsExpiration() {
            return 0;
        }

        @Override
        public void setEventsExpiration(long l) {

        }

        @Override
        public Stream<String> getEventsListenersStream() {
            return Stream.empty();
        }

        @Override
        public void setEventsListeners(Set<String> set) {

        }

        @Override
        public Stream<String> getEnabledEventTypesStream() {
            return Stream.empty();
        }

        @Override
        public void setEnabledEventTypes(Set<String> set) {

        }

        @Override
        public boolean isAdminEventsEnabled() {
            return false;
        }

        @Override
        public void setAdminEventsEnabled(boolean b) {

        }

        @Override
        public boolean isAdminEventsDetailsEnabled() {
            return false;
        }

        @Override
        public void setAdminEventsDetailsEnabled(boolean b) {

        }

        @Override
        public ClientModel getMasterAdminClient() {
            return null;
        }

        @Override
        public void setMasterAdminClient(ClientModel clientModel) {

        }

        @Override
        public RoleModel getDefaultRole() {
            return null;
        }

        @Override
        public void setDefaultRole(RoleModel roleModel) {

        }

        @Override
        public ClientModel getAdminPermissionsClient() {
            return null;
        }

        @Override
        public void setAdminPermissionsClient(ClientModel clientModel) {

        }

        @Override
        public boolean isIdentityFederationEnabled() {
            return false;
        }

        @Override
        public boolean isInternationalizationEnabled() {
            return false;
        }

        @Override
        public void setInternationalizationEnabled(boolean b) {

        }

        @Override
        public Stream<String> getSupportedLocalesStream() {
            return Stream.empty();
        }

        @Override
        public void setSupportedLocales(Set<String> set) {

        }

        @Override
        public String getDefaultLocale() {
            return null;
        }

        @Override
        public void setDefaultLocale(String s) {

        }

        @Override
        public GroupModel createGroup(String s, String s1, GroupModel groupModel) {
            return null;
        }

        @Override
        public GroupModel getGroupById(String s) {
            return null;
        }

        @Override
        public Stream<GroupModel> getGroupsStream() {
            return Stream.empty();
        }

        @Override
        public Long getGroupsCount(Boolean aBoolean) {
            return 0L;
        }

        @Override
        public Long getGroupsCountByNameContaining(String s) {
            return 0L;
        }

        @Override
        public Stream<GroupModel> getTopLevelGroupsStream() {
            return Stream.empty();
        }

        @Override
        public Stream<GroupModel> getTopLevelGroupsStream(Integer integer, Integer integer1) {
            return Stream.empty();
        }

        @Override
        public boolean removeGroup(GroupModel groupModel) {
            return false;
        }

        @Override
        public void moveGroup(GroupModel groupModel, GroupModel groupModel1) {

        }

        @Override
        public Stream<ClientScopeModel> getClientScopesStream() {
            return Stream.empty();
        }

        @Override
        public ClientScopeModel addClientScope(String s) {
            return null;
        }

        @Override
        public ClientScopeModel addClientScope(String s, String s1) {
            return null;
        }

        @Override
        public boolean removeClientScope(String s) {
            return false;
        }

        @Override
        public ClientScopeModel getClientScopeById(String s) {
            return null;
        }

        @Override
        public void addDefaultClientScope(ClientScopeModel clientScopeModel, boolean b) {

        }

        @Override
        public void removeDefaultClientScope(ClientScopeModel clientScopeModel) {

        }

        @Override
        public void createOrUpdateRealmLocalizationTexts(String s, Map<String, String> map) {

        }

        @Override
        public boolean removeRealmLocalizationTexts(String s) {
            return false;
        }

        @Override
        public Map<String, Map<String, String>> getRealmLocalizationTexts() {
            return Map.of();
        }

        @Override
        public Map<String, String> getRealmLocalizationTextsByLocale(String s) {
            return Map.of();
        }

        @Override
        public Stream<ClientScopeModel> getDefaultClientScopesStream(boolean b) {
            return Stream.empty();
        }

        @Override
        public ClientInitialAccessModel createClientInitialAccessModel(int i, int i1) {
            return null;
        }

        @Override
        public ClientInitialAccessModel getClientInitialAccessModel(String s) {
            return null;
        }

        @Override
        public void removeClientInitialAccessModel(String s) {

        }

        @Override
        public Stream<ClientInitialAccessModel> getClientInitialAccesses() {
            return Stream.empty();
        }

        @Override
        public void decreaseRemainingCount(ClientInitialAccessModel clientInitialAccessModel) {

        }
    }
}