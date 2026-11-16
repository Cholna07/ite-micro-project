package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class RevokeUserSessionUseCase {
    private final UserSessionRepository sessions;
    public RevokeUserSessionUseCase(UserSessionRepository sessions) { this.sessions = sessions; }

    public boolean execute(UUID userUuid, UUID sessionId, UUID actorUuid, String reason) {
        return sessions.revoke(sessionId, userUuid, actorUuid, Instant.now(), reason);
    }

    public int revokeOthers(UUID userUuid, UUID currentSessionId, UUID actorUuid) {
        if (currentSessionId == null) throw new IllegalStateException("Current session could not be identified");
        return sessions.revokeOthers(userUuid, currentSessionId, actorUuid, Instant.now(), "REVOKED_OTHER_SESSIONS");
    }

    public int revokeAll(UUID userUuid, UUID actorUuid) {
        return sessions.revokeAll(userUuid, actorUuid, Instant.now(), "REVOKED_BY_ADMIN");
    }
}
