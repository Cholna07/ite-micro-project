package kh.edu.istad.platform.iam.domain.port.out;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.UserSession;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository {
    UserSession save(UserSession session);

    Optional<UserSession> findById(UUID sessionId);

    boolean isActive(UUID sessionId, UUID userUuid, Instant now);

    PageResult<UserSession> findActiveByUser(UUID userUuid, Instant now, int page, int size);

    boolean revoke(UUID sessionId, UUID userUuid, UUID revokedByUserUuid, Instant revokedAt, String reason);

    int revokeOthers(UUID userUuid, UUID currentSessionId, UUID revokedByUserUuid, Instant revokedAt, String reason);

    int revokeAll(UUID userUuid, UUID revokedByUserUuid, Instant revokedAt, String reason);

    boolean updateActivity(UUID sessionId, Instant lastActivityAt, Instant expiresAt);

    boolean touchActivity(UUID sessionId, UUID userUuid, Instant lastActivityAt);
}
