package kh.edu.istad.platform.iam.domain.dto.result;

import java.util.UUID;

public record UserSummary(
        UUID uuid,
        String username,
        String email,
        String givenName,
        String familyName,
        boolean enabled,
        boolean emailVerified
) {
}
