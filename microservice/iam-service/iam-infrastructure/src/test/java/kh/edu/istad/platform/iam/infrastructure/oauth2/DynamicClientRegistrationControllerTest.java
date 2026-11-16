package kh.edu.istad.platform.iam.infrastructure.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DynamicClientRegistrationControllerTest {
    @Test
    void existingDcrStillGeneratesSecretAndRegistrationTokenOnlyForInitialResponse() {
        RegisteredClientRepository repository = mock(RegisteredClientRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenAnswer(invocation -> "encoded:" + invocation.getArgument(0));
        when(encoder.matches(any(), any())).thenReturn(true);
        var controller = new DynamicClientRegistrationController(repository, encoder);
        var request = new DynamicClientRegistrationController.RegistrationRequest("DCR app",
                List.of("https://example.org/oauth/callback"), List.of("authorization_code", "refresh_token"),
                List.of("code"), List.of("openid", "profile", "email"), "client_secret_basic");
        var authentication = new UsernamePasswordAuthenticationToken("admin", "n/a",
                List.of(new SimpleGrantedAuthority("client.create")));
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setScheme("https"); servletRequest.setServerName("iam.example"); servletRequest.setRequestURI("/connect/register");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
        var response = controller.registerClient(authentication, request);
        RequestContextHolder.resetRequestAttributes();
        assertEquals(201, response.getStatusCode().value());
        var body = response.getBody(); assertNotNull(body); assertNotNull(body.client_secret()); assertNotNull(body.registration_access_token());
        var captor = org.mockito.ArgumentCaptor.forClass(RegisteredClient.class);
        verify(repository).save(captor.capture());
        assertEquals("encoded:" + body.client_secret(), captor.getValue().getClientSecret());
        Object tokenHash = captor.getValue().getClientSettings().getSetting("registration_access_token_hash");
        assertEquals("encoded:" + body.registration_access_token(), tokenHash);

        when(repository.findByClientId(body.client_id())).thenReturn(captor.getValue());
        var detail = controller.getRegisteredClient(body.client_id(), "Bearer " + body.registration_access_token());
        assertEquals(body.client_id(), detail.client_id());
        assertFalse(java.util.Arrays.stream(detail.getClass().getRecordComponents()).anyMatch(component -> component.getName().contains("secret")));
    }

    @Test
    void dcrStillRejectsUnsupportedGrantTypes() {
        var controller = new DynamicClientRegistrationController(mock(RegisteredClientRepository.class), mock(PasswordEncoder.class));
        var invalid = new DynamicClientRegistrationController.RegistrationRequest("app", List.of("https://example.org/cb"),
                List.of("implicit"), List.of("code"), List.of("openid"), "client_secret_basic");
        var auth = new UsernamePasswordAuthenticationToken("admin", "n/a", List.of(new SimpleGrantedAuthority("client.create")));
        assertThrows(ResponseStatusException.class, () -> controller.registerClient(auth, invalid));
    }
}
