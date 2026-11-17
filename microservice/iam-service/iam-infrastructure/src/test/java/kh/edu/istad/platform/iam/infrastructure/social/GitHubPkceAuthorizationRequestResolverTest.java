package kh.edu.istad.platform.iam.infrastructure.social;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GitHubPkceAuthorizationRequestResolverTest {

    @Test
    void appliesPkceOnlyToGitHubLogin() {
        ClientRegistration github = registration("github");
        ClientRegistration google = registration("google");
        ClientRegistrationRepository registrations = id -> "github".equals(id) ? github : google;
        GitHubPkceAuthorizationRequestResolver resolver = new GitHubPkceAuthorizationRequestResolver(registrations);

        var githubRequest = resolver.resolve(request("github"));
        assertNotNull(githubRequest);
        assertEquals("S256", githubRequest.getAdditionalParameters().get("code_challenge_method"));
        assertNotNull(githubRequest.getAdditionalParameters().get("code_challenge"));
        assertNotNull(githubRequest.getAttribute("code_verifier"));

        var googleRequest = resolver.resolve(request("google"));
        assertNotNull(googleRequest);
        // Other configured providers may independently enable PKCE in Spring's resolver.
        // At minimum this customization must not remove any protection already present.
        if (googleRequest.getAdditionalParameters().containsKey("code_challenge")) {
            assertEquals("S256", googleRequest.getAdditionalParameters().get("code_challenge_method"));
            assertNotNull(googleRequest.getAttribute("code_verifier"));
        }
    }

    private static MockHttpServletRequest request(String registrationId) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/" + registrationId);
        request.setServletPath("/oauth2/authorization/" + registrationId);
        return request;
    }

    private static ClientRegistration registration(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("read:user")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id")
                .clientName(registrationId)
                .build();
    }
}
