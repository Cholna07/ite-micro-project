package kh.edu.istad.platform.restapi.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LinkedIdentityResponse(
        UUID uuid,
        String provider,
        String email,
        boolean emailVerified,
        String displayName,
        String pictureUrl,
        Instant linkedAt,
        Instant lastLoginAt
) {
}
