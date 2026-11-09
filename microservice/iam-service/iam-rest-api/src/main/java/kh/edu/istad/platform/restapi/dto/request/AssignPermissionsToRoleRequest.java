package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record AssignPermissionsToRoleRequest(

        @NotEmpty
        Set<@NotNull UUID> permissionUuids

) {
}
