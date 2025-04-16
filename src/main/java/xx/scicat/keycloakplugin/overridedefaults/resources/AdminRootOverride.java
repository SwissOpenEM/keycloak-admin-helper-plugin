/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xx.scicat.keycloakplugin.overridedefaults.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.ext.Provider;
import org.keycloak.common.Profile;
import org.keycloak.http.HttpRequest;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.resources.admin.*;

/**
 * Root resource for admin console and admin REST API
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Provider
@Path("/admin")
public class AdminRootOverride extends AdminRoot {
//    protected static final Logger logger = Logger.getLogger(AdminRootOverride.class);
//
//    protected TokenManager tokenManager;
//
//    @Context
//    protected KeycloakSession session;
//
//    public AdminRootOverride() {
//        this.tokenManager = new TokenManager();
//    }
//
//    public static UriBuilder adminBaseUrl(UriInfo uriInfo) {
//        return adminBaseUrl(uriInfo.getBaseUriBuilder());
//    }
//
//    public static UriBuilder adminBaseUrl(UriBuilder base) {
//        return base.path(AdminRootOverride.class);
//    }
//
//    /**
//     * Convenience path to master realm admin console
//     *
//     * @exclude
//     * @return
//     */
//    @GET
//    @Operation(hidden = true)
//    public Response masterRealmAdminConsoleRedirect() {
//        String requestUrl = session.getContext().getUri().getRequestUri().toString();
//        KeycloakUriInfo adminUriInfo = session.getContext().getUri(UrlType.ADMIN);
//        String adminUrl = adminUriInfo.getBaseUri().toString();
//        String localAdminUrl = session.getContext().getUri(UrlType.LOCAL_ADMIN).getBaseUri().toString();
//
//        if (!isAdminConsoleEnabled() || (!requestUrl.startsWith(adminUrl) && !requestUrl.startsWith(localAdminUrl))) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//
//        RealmModel master = new RealmManager(session).getKeycloakAdminstrationRealm();
//        return Response.status(302).location(
//                adminUriInfo.getBaseUriBuilder().path(AdminRootOverride.class).path(AdminRootOverride.class, "getAdminConsole").path("/").build(master.getName())
//        ).build();
//    }
//
//    /**
//     * Convenience path to master realm admin console
//     *
//     * @exclude
//     * @return
//     */
//    @Path("index.{html:html}") // expression is actually "index.html" but this is a hack to get around jax-doclet bug
//    @GET
//    @Operation(hidden = true)
//    public Response masterRealmAdminConsoleRedirectHtml() {
//
//        if (!isAdminConsoleEnabled()) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//
//        return masterRealmAdminConsoleRedirect();
//    }
//
//    protected void resolveRealmAndUpdateSession(String name, KeycloakSession session) {
//        RealmManager realmManager = new RealmManager(session);
//        RealmModel realm = realmManager.getRealmByName(name);
//        if (realm == null) {
//            throw new NotFoundException("Realm not found.  Did you type in a bad URL?");
//        }
//        session.getContext().setRealm(realm);
//    }
//
//
//    public static UriBuilder adminConsoleUrl(UriInfo uriInfo) {
//        return adminConsoleUrl(uriInfo.getBaseUriBuilder());
//    }
//
//    public static UriBuilder adminConsoleUrl(UriBuilder base) {
//        return adminBaseUrl(base).path(AdminRootOverride.class, "getAdminConsole");
//    }
//
//    /**
//     * path to realm admin console ui
//     *
//     * @exclude
//     * @param name Realm name (not id!)
//     * @return
//     */
//    @Path("{realm}/console")
//    @Operation(hidden = true)
//    public AdminConsole getAdminConsole(final @PathParam("realm") String name) {
//
//        if (!isAdminConsoleEnabled()) {
//            throw new NotFoundException();
//        }
//
//        resolveRealmAndUpdateSession(name, session);
//
//        return new AdminConsole(session);
//    }
//
//
//    protected AdminAuth authenticateRealmAdminRequest(HttpHeaders headers) {
//        String tokenString = AppAuthManager.extractAuthorizationHeaderToken(headers);
//        if (tokenString == null) throw new NotAuthorizedException("Bearer");
//        AccessToken token;
//        try {
//            JWSInput input = new JWSInput(tokenString);
//            token = input.readJsonContent(AccessToken.class);
//        } catch (JWSInputException e) {
//            throw new NotAuthorizedException("Bearer token format error");
//        }
//        String realmName = Encode.decodePath(token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1));
//        RealmManager realmManager = new RealmManager(session);
//        RealmModel realm = realmManager.getRealmByName(realmName);
//        if (realm == null) {
//            throw new NotAuthorizedException("Unknown realm in token");
//        }
//        session.getContext().setRealm(realm);
//
//        AuthenticationManager.AuthResult authResult = new AppAuthManager.BearerTokenAuthenticator(session)
//                .setRealm(realm)
//                .setConnection(session.getContext().getConnection())
//                .setHeaders(headers)
//                .authenticate();
//
//        if (authResult == null) {
//            logger.debug("Token not valid");
//            throw new NotAuthorizedException("Bearer");
//        }
//
//        session.getContext().setBearerToken(authResult.getToken());
//
//        return new AdminAuth(realm, authResult.getToken(), authResult.getUser(), authResult.getClient());
//    }
//
//    public static UriBuilder realmsUrl(UriInfo uriInfo) {
//        return realmsUrl(uriInfo.getBaseUriBuilder());
//    }
//
//    public static UriBuilder realmsUrl(UriBuilder base) {
//        return adminBaseUrl(base).path(AdminRootOverride.class, "getRealmsAdmin");
//    }

    /**
     * Base Path to realm admin REST interface
     *
     * @param headers
     * @return
     */
    @Path("realms")
    @Override
    public RealmsAdminResource getRealmsAdmin() {
        HttpRequest request = getHttpRequest();

        if (!isAdminApiEnabled()) {
            throw new NotFoundException();
        }

        if (request.getHttpMethod().equals(HttpMethod.OPTIONS)) {
            return new RealmsAdminResourcePreflight(session, null, tokenManager, request);
        }

        AdminAuth auth = authenticateRealmAdminRequest(session.getContext().getRequestHeaders());
        if (auth != null) {
            if (logger.isDebugEnabled()) {
                logger.debugf("authenticated admin access for: %s", auth.getUser().getUsername());
            }
        }

        Cors.builder().allowedOrigins(auth.getToken()).allowedMethods("GET", "PUT", "POST", "DELETE").exposedHeaders("Location").auth().add();

        return new RealmsAdminResourceOverride(session, auth, tokenManager);
    }
//
//    @Path("{any:.*}")
//    @OPTIONS
//    @Operation(hidden = true)
//    public Object preFlight() {
//        if (!isAdminApiEnabled()) {
//            throw new NotFoundException();
//        }
//
//        return new AdminCorsPreflightService();
//    }
//
//    /**
//     * General information about the server
//     *
//     * @param headers
//     * @return
//     */
//    @Path("serverinfo")
//    public Object getServerInfo() {
//
//        if (!isAdminApiEnabled()) {
//            throw new NotFoundException();
//        }
//
//        HttpRequest request = getHttpRequest();
//
//        if (request.getHttpMethod().equals(HttpMethod.OPTIONS)) {
//            return new AdminCorsPreflightService();
//        }
//
//        AdminAuth auth = authenticateRealmAdminRequest(session.getContext().getRequestHeaders());
//        if (!AdminPermissions.realms(session, auth).isAdmin()) {
//            throw new ForbiddenException();
//        }
//
//        if (auth != null) {
//            logger.debugf("authenticated admin access for: %s", auth.getUser().getUsername());
//        }
//
//        Cors.builder().allowedOrigins(auth.getToken()).allowedMethods("GET", "PUT", "POST", "DELETE").auth().add();
//
//        return new ServerInfoAdminResource(session);
//    }
//
//    private HttpResponse getHttpResponse() {
//        return session.getContext().getHttpResponse();
//    }
//

    /**
     * copy of private orig
     */
    private HttpRequest getHttpRequest() {
        return session.getContext().getHttpRequest();
    }
//
//    public static Theme getTheme(KeycloakSession session, RealmModel realm) throws IOException {
//        return session.theme().getTheme(Theme.Type.ADMIN);
//    }
//
//    public static Properties getMessages(KeycloakSession session, RealmModel realm, String lang) {
//        try {
//            Theme theme = getTheme(session, realm);
//            Locale locale = lang != null ? Locale.forLanguageTag(lang) : Locale.ENGLISH;
//            return theme.getMessages(locale);
//        } catch (IOException e) {
//            logger.error("Failed to load messages from theme", e);
//            return new Properties();
//        }
//    }
//
//    public static Properties getMessages(KeycloakSession session, RealmModel realm, String lang, String... bundles) {
//        Properties compound = new Properties();
//        for (String bundle : bundles) {
//            Properties current = getMessages(session, realm, lang, bundle);
//            compound.putAll(current);
//        }
//        return compound;
//    }
//
//    private static Properties getMessages(KeycloakSession session, RealmModel realm, String lang, String bundle) {
//        try {
//            Theme theme = getTheme(session, realm);
//            Locale locale = lang != null ? Locale.forLanguageTag(lang) : Locale.ENGLISH;
//            return theme.getMessages(bundle, locale);
//        } catch (IOException e) {
//            logger.error("Failed to load messages from theme", e);
//            return new Properties();
//        }
//    }
//
    /**
     * copy of private orig
     */private static boolean isAdminApiEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.ADMIN_API);
    }
//
//    private static boolean isAdminConsoleEnabled() {
//        return Profile.isFeatureEnabled(Profile.Feature.ADMIN_V2);
//    }
}
