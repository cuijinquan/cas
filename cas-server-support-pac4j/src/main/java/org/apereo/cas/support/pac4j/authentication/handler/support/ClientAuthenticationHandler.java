package org.apereo.cas.support.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPac4jAuthenticationHandler;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;

/**
 * Pac4j authentication handler which gets the credentials and then the user profile
 * in a delegated authentication process from an external identity provider.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@RefreshScope
@Component("clientAuthenticationHandler")
@SuppressWarnings("unchecked")
public class ClientAuthenticationHandler extends AbstractPac4jAuthenticationHandler {

    /**
     * The clients for authentication.
     */
    
    @Autowired
    @Qualifier("builtClients")
    private Clients clients;

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && ClientCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final ClientCredential clientCredentials = (ClientCredential) credential;
        logger.debug("clientCredentials  {}", clientCredentials);

        final Credentials credentials = clientCredentials.getCredentials();
        final String clientName = credentials.getClientName();
        logger.debug("clientName:  {}", clientName);

        // get client
        final Client<Credentials, UserProfile> client = this.clients.findClient(clientName);
        logger.debug("client: {}", client);

        // web context
        final HttpServletRequest request = WebUtils.getHttpServletRequest();
        final HttpServletResponse response = WebUtils.getHttpServletResponse();
        final WebContext webContext = new J2EContext(request, response);

        // get user profile
        final UserProfile userProfile = client.getUserProfile(credentials, webContext);
        logger.debug("userProfile: {}", userProfile);

        return createResult(clientCredentials, userProfile);
    }

    public Clients getClients() {
        return this.clients;
    }

    public void setClients(final Clients clients) {
        this.clients = clients;
    }
}
