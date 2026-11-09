package kh.edu.istad.platform.iam.domain.dto.command;

import java.util.UUID;

public record ChangePasswordCommand(
        UUID userUuid,
        String currentPassword,
        String newPassword
) {
}
