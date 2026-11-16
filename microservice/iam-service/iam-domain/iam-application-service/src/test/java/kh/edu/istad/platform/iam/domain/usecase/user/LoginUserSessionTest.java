package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.LoginSessionContext;
import kh.edu.istad.platform.iam.domain.dto.command.LoginUserCommand;
import kh.edu.istad.platform.iam.domain.dto.result.AuthUserResult;
import kh.edu.istad.platform.iam.domain.dto.result.IssuedAccessToken;
import kh.edu.istad.platform.iam.domain.port.out.AccessTokenProvider;
import kh.edu.istad.platform.iam.domain.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.entity.UserSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUserSessionTest {
    @Mock LoadUserForAuthenticationUseCase loadUser;
    @Mock PasswordHasher passwordHasher;
    @Mock AccessTokenProvider accessTokenProvider;
    @Mock UserSessionRepository sessionRepository;

    @Test
    void createsPersistedSessionBeforeReturningJwtAndCapturesRequestMetadata() {
        UUID userUuid = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(900);
        when(loadUser.execute("sovann")).thenReturn(new AuthUserResult(userUuid, "sovann", null,
                "hash", true, false, List.of("ROLE_USER"), List.of("user.read")));
        when(passwordHasher.matches("password", "hash")).thenReturn(true);
        when(accessTokenProvider.generateAccessToken(eq(userUuid), eq("sovann"), any(), any(), any()))
                .thenAnswer(invocation -> new IssuedAccessToken("signed-jwt", expiresAt));
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        LoginUserUseCase useCase = new LoginUserUseCase(loadUser, passwordHasher, accessTokenProvider, sessionRepository);

        var result = useCase.execute(new LoginUserCommand("sovann", "password"),
                new LoginSessionContext("192.0.2.7", "Test Browser"));

        assertEquals("signed-jwt", result.accessToken());
        ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
        InOrder order = inOrder(accessTokenProvider, sessionRepository);
        order.verify(accessTokenProvider).generateAccessToken(eq(userUuid), eq("sovann"), any(), any(), any());
        order.verify(sessionRepository).save(sessionCaptor.capture());
        UserSession stored = sessionCaptor.getValue();
        assertEquals(userUuid, stored.userUuid());
        assertEquals("192.0.2.7", stored.ipAddress());
        assertEquals("Test Browser", stored.userAgent());
        assertEquals(expiresAt, stored.expiresAt());
    }

    @Test
    void doesNotIssueTokenForInvalidCredentials() {
        UUID userUuid = UUID.randomUUID();
        when(loadUser.execute("sovann")).thenReturn(new AuthUserResult(userUuid, "sovann", null,
                "hash", true, false, List.of(), List.of()));
        when(passwordHasher.matches("wrong", "hash")).thenReturn(false);
        LoginUserUseCase useCase = new LoginUserUseCase(loadUser, passwordHasher, accessTokenProvider, sessionRepository);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(
                new LoginUserCommand("sovann", "wrong"), new LoginSessionContext(null, null)));
        verifyNoInteractions(accessTokenProvider, sessionRepository);
    }
}
