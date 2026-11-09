package kh.edu.istad.platform.iam.domain.dto.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AssignPermissionsToRoleCommand(
        UUID roleUuid,
        List<UUID> permissionUuids,
        Instant expiresAt,
        String reason
) {
}
