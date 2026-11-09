package kh.edu.istad.platform.iam.domain.dto.command;

public record CreateRoleCommand(
        String name,
        String description
) {
}