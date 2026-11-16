package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.dto.command.AssignPermissionsToRoleCommand;
import kh.edu.istad.platform.iam.domain.dto.result.PermissionAssignmentResult;
import kh.edu.istad.platform.iam.domain.exception.RolePermissionAlreadyAssignedException;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.RolePermission;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RolePermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Set;
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

    @Transactional
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

        Set<UUID> seenPermissionUuids = new HashSet<>();
        for (UUID permissionUuid : command.permissionUuids()) {
            if (permissionUuid == null) {
                throw new IllegalArgumentException("Permission UUID cannot be null");
            }
            if (!seenPermissionUuids.add(permissionUuid)) {
                throw new RolePermissionAlreadyAssignedException("Duplicate permission UUIDs are not allowed");
            }
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
                throw new RolePermissionAlreadyAssignedException(
                        "Permission is already assigned to this role: "
                                + permissionUuid
                );
            }

            permissions.add(permission);
        }

        Instant assignedAt = Instant.now();
        String assignedBy = currentActorProvider.getCurrentActor();
        for (Permission permission : permissions) {
            rolePermissionRepository.revokeExpiredAssignments(role.getId(), permission.getId(), assignedAt, assignedBy);
        }

        // Save permission assignments only after the complete request validates.
        List<PermissionAssignmentResult> results = new ArrayList<>(permissions.size());

        for (Permission permission : permissions) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(permission.getId());
            rolePermission.setAssignedAt(assignedAt);
            rolePermission.setAssignedBy(assignedBy);
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
