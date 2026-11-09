package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.domain.dto.command.AssignPermissionsToRoleCommand;
import kh.edu.istad.platform.iam.domain.dto.command.CreateRoleCommand;
import kh.edu.istad.platform.iam.domain.dto.command.UpdateRoleCommand;
import kh.edu.istad.platform.iam.domain.dto.query.RoleFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.dto.result.PermissionAssignmentResult;
import kh.edu.istad.platform.iam.domain.dto.result.RoleResult;
import kh.edu.istad.platform.iam.domain.dto.result.RoleSummary;
import kh.edu.istad.platform.iam.domain.usecase.role.ActivateRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.AssignPermissionsToRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.CreateRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.DeactivateRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.DeleteRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.GetRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.ListRolesUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.RevokePermissionFromRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.UpdateRoleUseCase;
import kh.edu.istad.platform.restapi.dto.request.AssignPermissionsToRoleRequest;
import kh.edu.istad.platform.restapi.dto.request.CreateRoleRequest;
import kh.edu.istad.platform.restapi.dto.request.UpdateRoleRequest;
import kh.edu.istad.platform.restapi.dto.response.PageMetadata;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import kh.edu.istad.platform.restapi.dto.response.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final CreateRoleUseCase createRoleUseCase;
    private final ListRolesUseCase listRolesUseCase;
    private final GetRoleUseCase getRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final ActivateRoleUseCase activateRoleUseCase;
    private final DeactivateRoleUseCase deactivateRoleUseCase;
    private final AssignPermissionsToRoleUseCase assignPermissionsToRoleUseCase;
    private final RevokePermissionFromRoleUseCase revokePermissionFromRoleUseCase;

    // 1. Create role
    @PostMapping
    public ResponseEntity<RoleResponse> createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        CreateRoleCommand command = new CreateRoleCommand(
                request.name(),
                request.description()
        );

        RoleResult result = createRoleUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(result));
    }

    // 2. List roles
    @GetMapping
    public ResponseEntity<PageResponse<RoleResponse>> listRoles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean systemRole,
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        RoleFilterQuery query = new RoleFilterQuery(
                keyword,
                active,
                systemRole,
                deleted,
                page,
                size,
                sortBy,
                sortDirection
        );

        PageResult<RoleSummary> result = listRolesUseCase.execute(query);

        List<RoleResponse> content = result.content()
                .stream()
                .map(this::toResponse)
                .toList();

        PageMetadata metadata = new PageMetadata(
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.first(),
                result.last(),
                content.isEmpty()
        );

        return ResponseEntity.ok(
                new PageResponse<>(content, metadata)
        );
    }

    // 3. Get role by UUID
    @GetMapping("/{uuid}")
    public ResponseEntity<RoleResponse> getRole(
            @PathVariable UUID uuid
    ) {
        RoleResult result = getRoleUseCase.execute(uuid);

        return ResponseEntity.ok(toResponse(result));
    }

    // 4. Update role
    @PutMapping("/{uuid}")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        UpdateRoleCommand command = new UpdateRoleCommand(
                uuid,
                request.name(),
                request.description()
        );

        RoleResult result = updateRoleUseCase.execute(command);

        return ResponseEntity.ok(toResponse(result));
    }

    // 5. Delete role
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteRole(
            @PathVariable UUID uuid
    ) {
        deleteRoleUseCase.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    // 6. Activate role
    @PutMapping("/{uuid}/activate")
    public ResponseEntity<Void> activateRole(
            @PathVariable UUID uuid
    ) {
        activateRoleUseCase.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    // 7. Deactivate role
    @PutMapping("/{uuid}/deactivate")
    public ResponseEntity<Void> deactivateRole(
            @PathVariable UUID uuid
    ) {
        deactivateRoleUseCase.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    // 8. Assign permissions to role
    @PostMapping("/{uuid}/permissions")
    public ResponseEntity<List<PermissionAssignmentResult>> assignPermissions(
            @PathVariable UUID uuid,
            @Valid @RequestBody AssignPermissionsToRoleRequest request
    ) {
        AssignPermissionsToRoleCommand command =
                new AssignPermissionsToRoleCommand(
                        uuid,
                        List.copyOf(request.permissionUuids()),
                        null,
                        null
                );

        List<PermissionAssignmentResult> result =
                assignPermissionsToRoleUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 9. Revoke permission from role
    @DeleteMapping("/{uuid}/permissions/{permissionUuid}")
    public ResponseEntity<Void> revokePermission(
            @PathVariable UUID uuid,
            @PathVariable UUID permissionUuid,
            @RequestParam(required = false) String reason
    ) {
        revokePermissionFromRoleUseCase.execute(
                uuid,
                permissionUuid,
                reason
        );

        return ResponseEntity.noContent().build();
    }

    // Map RoleResult to REST response
    private RoleResponse toResponse(RoleResult result) {
        return new RoleResponse(
                result.uuid(),
                result.name(),
                result.description(),
                result.active(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    // Map RoleSummary to REST response
    private RoleResponse toResponse(RoleSummary result) {
        return new RoleResponse(
                result.uuid(),
                result.name(),
                result.description(),
                result.active(),
                null,
                null
        );
    }
}
