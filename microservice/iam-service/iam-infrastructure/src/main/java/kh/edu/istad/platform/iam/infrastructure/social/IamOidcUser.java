package kh.edu.istad.platform.iam.infrastructure.social;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;

/** Keeps provider claims and ID token intact while using IAM's username as the local principal name. */
public final class IamOidcUser implements OidcUser {
    private final OidcUser delegate;
    private final String username;

    public IamOidcUser(OidcUser delegate, String username) {
        this.delegate = delegate;
        this.username = username;
    }

    @Override public Map<String, Object> getClaims() { return delegate.getClaims(); }
    @Override public OidcUserInfo getUserInfo() { return delegate.getUserInfo(); }
    @Override public OidcIdToken getIdToken() { return delegate.getIdToken(); }
    @Override public Map<String, Object> getAttributes() { return delegate.getAttributes(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return delegate.getAuthorities(); }
    @Override public String getName() { return username; }
}
