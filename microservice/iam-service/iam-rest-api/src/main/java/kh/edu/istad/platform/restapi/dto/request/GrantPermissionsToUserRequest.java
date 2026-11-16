package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GrantPermissionsToUserRequest(
        @NotEmpty List<UUID> permissionUuids,
        @NotBlank String effect,
        Instant expiresAt,
        String reason
) {
}
