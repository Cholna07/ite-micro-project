package kh.edu.istad.platform.iam.domain.dto.command;

public record CreatePermissionCommand(
        String name,
        String resource,
        String action,
        String description
) {
}
