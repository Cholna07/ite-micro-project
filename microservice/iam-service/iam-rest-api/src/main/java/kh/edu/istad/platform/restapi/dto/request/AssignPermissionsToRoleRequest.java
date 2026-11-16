package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AssignPermissionsToRoleRequest(

        @NotEmpty
        List<@NotNull UUID> permissionUuids

) {
}
