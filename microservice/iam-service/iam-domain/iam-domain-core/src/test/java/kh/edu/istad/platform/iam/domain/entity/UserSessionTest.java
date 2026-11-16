package kh.edu.istad.platform.iam.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {
    @Test
    void sessionIsActiveOnlyUntilExpiryAndBeforeRevocation() {
        Instant now = Instant.parse("2026-11-17T00:00:00Z");
        UserSession active = session(now, now.plusSeconds(60), null);
        UserSession revoked = session(now, now.plusSeconds(60), now.minusSeconds(1));
        UserSession expired = session(now.minusSeconds(120), now.minusSeconds(1), null);

        assertTrue(active.isActiveAt(now));
        assertFalse(revoked.isActiveAt(now));
        assertFalse(expired.isActiveAt(now));
        assertFalse(active.isActiveAt(active.expiresAt()));
    }

    @Test
    void nullExpirationRepresentsAnUnboundedServletTimeout() {
        Instant now = Instant.parse("2026-11-17T00:00:00Z");
        UserSession session = session(now, null, null);
        assertTrue(session.isActiveAt(now.plusSeconds(10_000)));
    }

    private UserSession session(Instant created, Instant expires, Instant revokedAt) {
        return new UserSession(UUID.randomUUID(), UUID.randomUUID(), SessionCredentialType.JWT,
                "PASSWORD", null, created, created, expires, null, null, revokedAt, null,
                revokedAt == null ? null : "USER_REQUEST");
    }
}
