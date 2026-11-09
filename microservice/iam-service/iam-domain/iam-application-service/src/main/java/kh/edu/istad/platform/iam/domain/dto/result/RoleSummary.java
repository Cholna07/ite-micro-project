package kh.edu.istad.platform.iam.domain.dto.result;

import java.util.UUID;

public record RoleSummary(
        UUID uuid,
        String name,
        String description,
        boolean system,
        boolean active
) {
}
