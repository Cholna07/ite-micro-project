package kh.edu.istad.platform.iam.infrastructure.social;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SocialLoginProvidersControllerTest {
    @Test
    void returnsOnlyConfiguredSupportedProvidersWithoutExposingCredentials() {
        var google = registration("google");
        var telegram = registration("telegram");
        ClientRegistrationRepository repository = mock(ClientRegistrationRepository.class);
        when(repository.findByRegistrationId("google")).thenReturn(google);
        when(repository.findByRegistrationId("telegram")).thenReturn(telegram);
        when(repository.findByRegistrationId("github")).thenReturn(null);
        when(repository.findByRegistrationId("facebook")).thenReturn(null);
        @SuppressWarnings("unchecked")
        ObjectProvider<ClientRegistrationRepository> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(repository);

        assertEquals(List.of("google", "telegram"), new SocialLoginProvidersController(provider).listEnabledProviders());
    }

    private static ClientRegistration registration(String id) {
        return ClientRegistration.withRegistrationId(id)
                .clientId("public-id")
                .clientSecret("secret")
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://provider.example/authorize")
                .tokenUri("https://provider.example/token")
                .userInfoUri("https://provider.example/userinfo")
                .userNameAttributeName("sub")
                .clientName(id)
                .build();
    }
}
