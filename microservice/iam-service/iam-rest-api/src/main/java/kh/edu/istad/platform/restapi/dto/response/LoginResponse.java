package kh.edu.istad.platform.restapi.dto.response;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        UUID userUuid,
        String username,
        List<String> roles,
        List<String> permissions
) {
}