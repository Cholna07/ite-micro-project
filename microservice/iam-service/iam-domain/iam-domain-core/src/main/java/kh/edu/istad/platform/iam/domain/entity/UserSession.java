package kh.edu.istad.platform.iam.domain.entity;

import java.time.Instant;
import java.util.UUID;

public record UserSession(
        UUID id,
        UUID userUuid,
        SessionCredentialType credentialType,
        String authenticationType,
        String provider,
        Instant createdAt,
        Instant lastActivityAt,
        Instant expiresAt,
        String ipAddress,
        String userAgent,
        Instant revokedAt,
        UUID revokedByUserUuid,
        String revocationReason
) {
    public boolean isActiveAt(Instant now) {
        return revokedAt == null && (expiresAt == null || expiresAt.isAfter(now));
    }
}
