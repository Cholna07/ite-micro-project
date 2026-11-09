package kh.edu.istad.platform.iam.domain.dto.result;

import java.util.List;
import java.util.UUID;

public record LoginResult(
        String accessToken,
        String tokenType,
        UUID userUuid,
        String username,
        List<String> roles,
        List<String> permissions
) {
}