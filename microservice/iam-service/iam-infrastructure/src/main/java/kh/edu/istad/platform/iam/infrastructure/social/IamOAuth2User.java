package kh.edu.istad.platform.iam.infrastructure.social;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/** Keeps provider attributes intact while using IAM's username as the local principal name. */
public final class IamOAuth2User implements OAuth2User {
    private final OAuth2User delegate;
    private final String username;

    public IamOAuth2User(OAuth2User delegate, String username) {
        this.delegate = delegate;
        this.username = username;
    }

    @Override public Map<String, Object> getAttributes() { return delegate.getAttributes(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return delegate.getAuthorities(); }
    @Override public String getName() { return username; }
}
