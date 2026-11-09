package kh.edu.istad.platform.iam.domain.dto.result;

import java.time.Instant;
import java.util.UUID;

public record RoleResult(
        UUID uuid,
        String name,
        String description,
        boolean system,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
