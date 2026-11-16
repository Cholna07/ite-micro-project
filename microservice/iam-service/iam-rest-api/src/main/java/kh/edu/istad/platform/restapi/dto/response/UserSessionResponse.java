package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.domain.entity.SessionCredentialType;
import java.time.Instant;
import java.util.UUID;

public record UserSessionResponse(UUID id, SessionCredentialType credentialType, String authenticationType,
                                 String provider, Instant createdAt, Instant lastActivityAt, Instant expiresAt,
                                 String ipAddress, String userAgent, boolean current) {
}
