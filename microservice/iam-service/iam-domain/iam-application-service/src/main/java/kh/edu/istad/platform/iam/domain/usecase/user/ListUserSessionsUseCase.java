package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.UserSession;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ListUserSessionsUseCase {
    private final UserSessionRepository sessions;
    public ListUserSessionsUseCase(UserSessionRepository sessions) { this.sessions = sessions; }

    public PageResult<UserSession> execute(UUID userUuid, int page, int size) {
        if (userUuid == null) throw new IllegalArgumentException("User is required");
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid pagination parameters");
        return sessions.findActiveByUser(userUuid, Instant.now(), page, size);
    }
}
