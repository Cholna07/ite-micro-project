package kh.edu.istad.platform.iam.domain.dto.result;

import java.time.Instant;
import java.util.UUID;

public record UserResult(
        UUID uuid,
        String username,
        String email,
        String givenName,
        String familyName,
        String middleName,
        String nickname,
        String gender,
        String phoneNumber,
        boolean emailVerified,
        boolean phoneNumberVerified,
        boolean enabled,
        boolean accountLocked,
        Instant createdAt,
        Instant updatedAt
) {
}
