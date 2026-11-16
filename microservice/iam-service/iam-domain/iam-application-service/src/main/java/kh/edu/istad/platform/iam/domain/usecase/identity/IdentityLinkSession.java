package kh.edu.istad.platform.iam.domain.usecase.identity;

import java.util.UUID;

/** Session binding used only during an explicitly initiated OAuth identity link. */
public final class IdentityLinkSession {
    public static final String USER_UUID_ATTRIBUTE = "IAM_PENDING_IDENTITY_LINK_USER_UUID";

    private IdentityLinkSession() {
    }

    public static UUID parseUserUuid(Object value) {
        if (!(value instanceof String text)) return null;
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
