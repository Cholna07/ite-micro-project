package kh.edu.istad.platform.iam.domain.dto.command;

import java.util.UUID;

public record UpdateRoleCommand(
        UUID roleUuid,
        String name,
        String description
) {
}
