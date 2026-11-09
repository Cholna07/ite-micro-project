package kh.edu.istad.platform.iam.domain.dto.result;

import java.time.Instant;
import java.util.UUID;

public record IdentityResult(
        UUID uuid,
        String provider,
        String providerSubject,
        String email,
        boolean emailVerified,
        String displayName,
        String pictureUrl,
        Instant linkedAt,
        Instant lastLoginAt
) {
}
