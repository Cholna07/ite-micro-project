package kh.edu.istad.platform.iam.infrastructure.social;

import kh.edu.istad.platform.iam.domain.usecase.identity.IdentityLinkSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

final class IdentityLinkRequestContext {
    private IdentityLinkRequestContext() {
    }

    static UUID pendingUserUuid() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        var session = attributes.getRequest().getSession(false);
        return session == null ? null : IdentityLinkSession.parseUserUuid(
                session.getAttribute(IdentityLinkSession.USER_UUID_ATTRIBUTE));
    }
}
