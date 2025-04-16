

package xx.scicat.keycloakplugin;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class EmailLinkAuthenticator implements Authenticator {
    private static final Logger LOG = Logger.getLogger(EmailLinkAuthenticator.class);

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // TODO Auto-generated method stub
        boolean allow = Math.random() < 0.3;
        LOG.warn("action called! %s %s %s  -> allow=%s".formatted(context.getUser(), context.getFlowPath(), context, allow));
        if (allow)
            context.success();
        else
            context.failure(AuthenticationFlowError.ACCESS_DENIED);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        // TODO Auto-generated method stub
        boolean allow = Math.random() < 0.3;
        LOG.warn("authenticate called! %s %s %s  -> allow=%s".formatted(context.getUser(), context.getFlowPath(), context, allow));
        if (allow)
            context.success();
        else
            context.failure(AuthenticationFlowError.ACCESS_DENIED);
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // TODO Auto-generated method stub
        return user.getEmail() != null;
    }

    @Override
    public boolean requiresUser() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // TODO Auto-generated method stub
    }

}


