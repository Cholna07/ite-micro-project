package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.RolePermission;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RolePermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class RevokePermissionFromRoleUseCase {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final CurrentActorProvider currentActorProvider;

    public RevokePermissionFromRoleUseCase(
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

    public void execute(
            UUID roleUuid,
            UUID permissionUuid,
            String reason
    ) {

        if (roleUuid == null || permissionUuid == null) {
            throw new IllegalArgumentException(
                    "Role UUID and Permission UUID are required"
            );
        }

        Role role = roleRepository.findByUuid(roleUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("Role not found"));

        Permission permission = permissionRepository
                .findByUuid(permissionUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("Permission not found"));

        RolePermission assignment = rolePermissionRepository
                .findActiveAssignment(role.getId(), permission.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Active permission assignment not found"
                        ));

        assignment.setRevokedAt(Instant.now());
        assignment.setRevokedBy(
                currentActorProvider.getCurrentActor()
        );
        assignment.setReason(reason);

        rolePermissionRepository.save(assignment);
    }
}



