package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.HttpSessionInventory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityCurrentActorProvider
        implements CurrentActorProvider {

    private final UserRepository users;
    private final UserSessionRepository sessions;

    public SpringSecurityCurrentActorProvider(UserRepository users, UserSessionRepository sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    @Override
    public String getCurrentActor() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return "SYSTEM";
        }

        String principalName = authentication.getName();
        try {
            java.util.UUID.fromString(principalName);
            return principalName;
        } catch (IllegalArgumentException ignored) {
            if (users.findByUsernameOrEmail(principalName).isPresent()) return principalName;
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                var session = attributes.getRequest().getSession(false);
                Object marker = session == null ? null : session.getAttribute(HttpSessionInventory.SESSION_RECORD_ID);
                if (marker instanceof String value) {
                    try {
                        return sessions.findById(java.util.UUID.fromString(value))
                                .map(record -> record.userUuid().toString())
                                .orElse(principalName);
                    } catch (IllegalArgumentException ignoredMarker) {
                        return principalName;
                    }
                }
            }
            return principalName;
        }
    }
}
