package kh.edu.istad.platform.restapi.dto.response;

import java.time.Instant;
import java.util.UUID;

public record IdentityResponse(

        UUID uuid,
        UUID userUuid,
        String provider,
        String providerSubject,
        Instant createdAt,
        Instant updatedAt

) {
}
