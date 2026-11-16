package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.BrowserSessionContext;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.SessionCredentialType;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrowserSessionManagementTest {
    @Mock UserRepository users;
    @Mock UserSessionRepository sessions;

    @Test
    void registersSocialHttpSessionWithoutContainerSessionIdentifier() {
        UUID userUuid = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getUuid()).thenReturn(userUuid);
        when(users.findByUsernameOrEmail("local-user")).thenReturn(Optional.of(user));
        when(sessions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var useCase = new RegisterBrowserSessionUseCase(users, sessions);

        UUID id = useCase.execute("local-user", new BrowserSessionContext("192.0.2.9", "Browser", "SOCIAL", "google", 1800));

        ArgumentCaptor<kh.edu.istad.platform.iam.domain.entity.UserSession> captor =
                ArgumentCaptor.forClass(kh.edu.istad.platform.iam.domain.entity.UserSession.class);
        verify(sessions).save(captor.capture());
        var stored = captor.getValue();
        assertEquals(id, stored.id());
        assertEquals(userUuid, stored.userUuid());
        assertEquals(SessionCredentialType.HTTP_SESSION, stored.credentialType());
        assertEquals("SOCIAL", stored.authenticationType());
        assertEquals("google", stored.provider());
        assertNotNull(stored.expiresAt());
        assertNull(stored.revokedAt());
    }

    @Test
    void revokeOthersPreservesTheCurrentSessionAndOwnershipIsPassedToRepository() {
        UUID userUuid = UUID.randomUUID();
        UUID currentId = UUID.randomUUID();
        when(sessions.revokeOthers(eq(userUuid), eq(currentId), eq(userUuid), any(), eq("REVOKED_OTHER_SESSIONS")))
                .thenReturn(1);
        var useCase = new RevokeUserSessionUseCase(sessions);

        assertEquals(1, useCase.revokeOthers(userUuid, currentId, userUuid));
        verify(sessions).revokeOthers(eq(userUuid), eq(currentId), eq(userUuid), any(), any());
    }

    @Test
    void refusesToRevokeOthersWhenCurrentSessionCannotBeIdentified() {
        var useCase = new RevokeUserSessionUseCase(sessions);
        assertThrows(IllegalStateException.class, () -> useCase.revokeOthers(UUID.randomUUID(), null, UUID.randomUUID()));
        verifyNoInteractions(sessions);
    }
}
