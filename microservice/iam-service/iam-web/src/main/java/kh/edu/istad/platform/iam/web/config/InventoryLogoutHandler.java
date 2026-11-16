package kh.edu.istad.platform.iam.web.config;

import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.HttpSessionInventory;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.time.Instant;
import java.util.UUID;

public final class InventoryLogoutHandler implements LogoutHandler {
    private static final Logger log = LoggerFactory.getLogger(InventoryLogoutHandler.class);
    private final UserSessionRepository sessions;
    private final RecordAccountActivityEventUseCase recordActivity;

    public InventoryLogoutHandler(UserSessionRepository sessions, RecordAccountActivityEventUseCase recordActivity) {
        this.sessions = sessions;
        this.recordActivity = recordActivity;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        var httpSession = request.getSession(false);
        if (httpSession == null) return;
        Object value = httpSession.getAttribute(HttpSessionInventory.SESSION_RECORD_ID);
        if (!(value instanceof String text)) return;
        try {
            UUID id = UUID.fromString(text);
            sessions.findById(id).ifPresent(record -> {
                boolean revoked = sessions.revoke(id, record.userUuid(), record.userUuid(), Instant.now(), "LOGOUT");
                if (revoked) {
                    try {
                        recordActivity.execute(record.userUuid(), AccountActivityEventType.CURRENT_SESSION_REVOKED,
                                "Current session revoked");
                    } catch (RuntimeException exception) {
                        log.error("Session was revoked, but its account activity event could not be recorded", exception);
                    }
                }
            });
        } catch (RuntimeException exception) {
            log.error("Could not revoke browser session inventory record during logout", exception);
        }
    }
}
