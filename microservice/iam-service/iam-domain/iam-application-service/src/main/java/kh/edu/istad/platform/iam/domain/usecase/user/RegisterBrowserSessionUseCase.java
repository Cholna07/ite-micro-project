package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.BrowserSessionContext;
import kh.edu.istad.platform.iam.domain.entity.SessionCredentialType;
import kh.edu.istad.platform.iam.domain.entity.UserSession;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class RegisterBrowserSessionUseCase {
    private final UserRepository users;
    private final UserSessionRepository sessions;

    public RegisterBrowserSessionUseCase(UserRepository users, UserSessionRepository sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    public UUID execute(String username, BrowserSessionContext context) {
        var user = users.findByUsernameOrEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated local user was not found"));
        Instant now = Instant.now();
        Integer timeout = context.maxInactiveIntervalSeconds();
        Instant expiresAt = timeout == null || timeout <= 0 ? null : now.plusSeconds(timeout);
        UUID id = UUID.randomUUID();
        sessions.save(new UserSession(id, user.getUuid(), SessionCredentialType.HTTP_SESSION,
                context.authenticationType(), context.provider(), now, now, expiresAt,
                truncate(context.ipAddress(), 45), truncate(context.userAgent(), 512), null, null, null));
        return id;
    }

    private static String truncate(String value, int max) {
        if (value == null || value.isBlank()) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
