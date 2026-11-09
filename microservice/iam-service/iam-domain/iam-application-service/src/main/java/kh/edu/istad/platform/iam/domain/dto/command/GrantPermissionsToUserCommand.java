package kh.edu.istad.platform.iam.domain.dto.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GrantPermissionsToUserCommand(
        UUID userUuid,
        List<UUID> permissionUuids,
        String effect,
        Instant expiresAt,
        String reason
) {
}