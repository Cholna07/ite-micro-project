package kh.edu.istad.platform.restapi.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RoleResponse(

        UUID uuid,
        String name,
        String description,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt

) {
}
