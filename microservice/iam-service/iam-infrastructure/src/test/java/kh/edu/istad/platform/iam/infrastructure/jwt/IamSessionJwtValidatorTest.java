package kh.edu.istad.platform.iam.infrastructure.jwt;

import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IamSessionJwtValidatorTest {
    private final UserSessionRepository sessions = mock(UserSessionRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final IamSessionJwtValidator validator = new IamSessionJwtValidator(sessions, users);

    @Test
    void acceptsActiveTokenForEnabledUser() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(sessions.isActive(eq(sessionId), eq(userId), any())).thenReturn(true);
        when(sessions.touchActivity(eq(sessionId), eq(userId), any())).thenReturn(true);
        when(users.findByUuid(userId)).thenReturn(Optional.of(user(userId, true, true)));

        assertFalse(validator.validate(iamJwt(userId, sessionId)).hasErrors());
    }

    @Test
    void rejectsExistingTokenAfterAccountIsDisabledEvenWhenSessionIsActive() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(sessions.isActive(eq(sessionId), eq(userId), any())).thenReturn(true);
        when(users.findByUuid(userId)).thenReturn(Optional.of(user(userId, false, true)));

        assertTrue(validator.validate(iamJwt(userId, sessionId)).hasErrors());
        verify(sessions, never()).touchActivity(any(), any(), any());
    }

    @Test
    void rejectsExistingTokenForLockedUser() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(sessions.isActive(eq(sessionId), eq(userId), any())).thenReturn(true);
        when(users.findByUuid(userId)).thenReturn(Optional.of(user(userId, true, false)));

        assertTrue(validator.validate(iamJwt(userId, sessionId)).hasErrors());
        verify(sessions, never()).touchActivity(any(), any(), any());
    }

    @Test
    void failsClosedWhenUserCannotBeLoadedButLeavesOtherOauthTokensAlone() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(sessions.isActive(eq(sessionId), eq(userId), any())).thenReturn(true);
        when(users.findByUuid(userId)).thenReturn(Optional.empty());
        assertTrue(validator.validate(iamJwt(userId, sessionId)).hasErrors());

        clearInvocations(sessions, users);
        Jwt oauthToken = Jwt.withTokenValue("oauth-token").header("alg", "RS256")
                .subject(userId.toString()).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        assertFalse(validator.validate(oauthToken).hasErrors());
        verifyNoInteractions(sessions, users);
    }

    private static User user(UUID id, boolean enabled, boolean unlocked) {
        User user = new User();
        user.setUuid(id);
        user.setEnabled(enabled);
        user.setDeleted(false);
        user.setAccountNonLocked(unlocked);
        return user;
    }

    private static Jwt iamJwt(UUID userId, UUID sessionId) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("signed-token").header("alg", "RS256").subject(userId.toString())
                .claim("username", "alice").claim("iam_token_type", "IAM_SESSION")
                .claim("iam_session_id", sessionId.toString()).issuedAt(now).expiresAt(now.plusSeconds(900)).build();
    }
}
