package kh.edu.istad.platform.iam.infrastructure.social;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/** Adds PKCE to GitHub's confidential OAuth login without changing other providers. */
final class GitHubPkceAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private static final String AUTHORIZATION_BASE_URI = "/oauth2/authorization";

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    GitHubPkceAuthorizationRequestResolver(ClientRegistrationRepository registrations) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(registrations, AUTHORIZATION_BASE_URI);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = this.delegate.resolve(request);
        if (authorizationRequest == null || !isGitHubRequest(request)) return authorizationRequest;
        OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.from(authorizationRequest);
        OAuth2AuthorizationRequestCustomizers.withPkce().accept(builder);
        return builder.build();
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = this.delegate.resolve(request, clientRegistrationId);
        if (authorizationRequest == null || !"github".equalsIgnoreCase(clientRegistrationId)) return authorizationRequest;
        OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.from(authorizationRequest);
        OAuth2AuthorizationRequestCustomizers.withPkce().accept(builder);
        return builder.build();
    }

    private static boolean isGitHubRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.endsWith(AUTHORIZATION_BASE_URI + "/github");
    }
}
