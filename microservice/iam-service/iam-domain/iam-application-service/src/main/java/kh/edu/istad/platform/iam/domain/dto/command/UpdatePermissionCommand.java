package kh.edu.istad.platform.iam.domain.dto.command;

import java.util.UUID;

public record UpdatePermissionCommand(
        UUID permissionUuid,
        String name,
        String resource,
        String action,
        String description
) {
}
