package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AssignRolesToUserRequest(
        @NotEmpty List<UUID> roleUuids,
        Instant expiresAt,
        String reason
) {
}
