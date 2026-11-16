package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.domain.entity.SessionCredentialType;
import kh.edu.istad.platform.iam.domain.entity.UserSession;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.HttpSessionInventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SpringSecurityCurrentActorProviderTest {
    private final UserRepository users = mock(UserRepository.class);
    private final UserSessionRepository sessions = mock(UserSessionRepository.class);

    @AfterEach void clearRequestAndSecurityContext() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesRenamedBrowserPrincipalThroughTheServerSessionInventory() {
        UUID inventoryId = UUID.randomUUID();
        UUID ownerUuid = UUID.randomUUID();
        Instant now = Instant.now();
        UserSession record = new UserSession(inventoryId, ownerUuid, SessionCredentialType.HTTP_SESSION,
                "PASSWORD", null, now, now, now.plusSeconds(600), null, null, null, null, null);
        when(users.findByUsernameOrEmail("previous-name")).thenReturn(Optional.empty());
        when(sessions.findById(inventoryId)).thenReturn(Optional.of(record));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(HttpSessionInventory.SESSION_RECORD_ID, inventoryId.toString());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("previous-name", "", java.util.List.of()));

        String actor = new SpringSecurityCurrentActorProvider(users, sessions).getCurrentActor();

        assertEquals(ownerUuid.toString(), actor);
    }
}
