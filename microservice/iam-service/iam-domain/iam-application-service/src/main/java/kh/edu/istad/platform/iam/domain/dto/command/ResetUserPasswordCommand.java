package kh.edu.istad.platform.iam.domain.dto.command;

import java.util.UUID;

public record ResetUserPasswordCommand(
        UUID userUuid,
        String newPassword
) {
}
