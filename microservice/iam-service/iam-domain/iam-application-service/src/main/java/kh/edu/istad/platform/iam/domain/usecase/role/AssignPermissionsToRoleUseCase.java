package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.dto.command.AssignPermissionsToRoleCommand;
import kh.edu.istad.platform.iam.domain.dto.result.PermissionAssignmentResult;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.RolePermission;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RolePermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
@Component
public class AssignPermissionsToRoleUseCase {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final CurrentActorProvider currentActorProvider;

    public AssignPermissionsToRoleUseCase(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            CurrentActorProvider currentActorProvider
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.currentActorProvider = currentActorProvider;
    }

    public List<PermissionAssignmentResult> execute(
            AssignPermissionsToRoleCommand command
    ) {

        if (command == null || command.roleUuid() == null) {
            throw new IllegalArgumentException(
                    "Assign permission command and role UUID are required"
            );
        }

        if (command.permissionUuids() == null
                || command.permissionUuids().isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one permission is required"
            );
        }

        if (new HashSet<>(command.permissionUuids()).size()
                != command.permissionUuids().size()) {
            throw new IllegalArgumentException(
                    "Duplicate permission UUIDs are not allowed"
            );
        }

        Role role = roleRepository.findByUuid(command.roleUuid())
                .orElseThrow(() ->
                        new IllegalArgumentException("Role not found"));

        if (!role.isActive() || role.isDeleted()) {
            throw new IllegalStateException(
                    "Cannot assign permissions to an inactive role"
            );
        }

        // Validate all permissions before saving assignments
        List<Permission> permissions = new ArrayList<>();

        for (UUID permissionUuid : command.permissionUuids()) {

            Permission permission = permissionRepository
                    .findByUuid(permissionUuid)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Permission not found: " + permissionUuid
                            ));

            if (!permission.isActive() || permission.isDeleted()) {
                throw new IllegalStateException(
                        "Cannot assign an inactive permission: " + permissionUuid
                );
            }

            if (rolePermissionRepository.existsActiveAssignment(
                    role.getId(), permission.getId())) {
                throw new IllegalStateException(
                        "Permission is already assigned to this role: "
                                + permissionUuid
                );
            }

            permissions.add(permission);
        }

        // Save permission assignments
        List<PermissionAssignmentResult> results = new ArrayList<>();

        for (Permission permission : permissions) {

            Instant assignedAt = Instant.now();

            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(permission.getId());
            rolePermission.setAssignedAt(assignedAt);
            rolePermission.setAssignedBy(
                    currentActorProvider.getCurrentActor()
            );
            rolePermission.setExpiresAt(command.expiresAt());
            rolePermission.setReason(command.reason());

            rolePermissionRepository.save(rolePermission);

            results.add(new PermissionAssignmentResult(
                    role.getUuid(),
                    permission.getUuid(),
                    assignedAt,
                    command.expiresAt()
            ));
        }

        return results;
    }
}
