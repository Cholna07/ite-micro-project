package kh.edu.istad.platform.iam.domain.dto.result;

import java.util.List;
import java.util.UUID;

public record AuthUserResult(
        UUID uuid,
        String username,
        String email,
        String passwordHash,
        boolean enabled,
        boolean accountLocked,
        List<String> authorities,
        List<String> permissions
) {
}