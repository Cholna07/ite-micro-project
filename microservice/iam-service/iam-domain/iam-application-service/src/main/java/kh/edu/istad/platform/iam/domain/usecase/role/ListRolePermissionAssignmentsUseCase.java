package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.dto.result.RolePermissionAssignmentView;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RolePermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ListRolePermissionAssignmentsUseCase {
    private final RoleRepository roles;
    private final RolePermissionRepository assignments;
    private final PermissionRepository permissions;

    public ListRolePermissionAssignmentsUseCase(RoleRepository roles, RolePermissionRepository assignments, PermissionRepository permissions) {
        this.roles = roles;
        this.assignments = assignments;
        this.permissions = permissions;
    }

    public List<RolePermissionAssignmentView> execute(UUID roleUuid) {
        Role role = roles.findByUuid(roleUuid).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleUuid));
        Instant now = Instant.now();
        Map<Long, Permission> byId = permissions.findAll().stream().collect(Collectors.toMap(Permission::getId, Function.identity()));
        List<RolePermissionAssignmentView> result = new ArrayList<>();
        assignments.findByRoleId(role.getId()).stream()
                .filter(a -> a.getRevokedAt() == null && (a.getExpiresAt() == null || a.getExpiresAt().isAfter(now)))
                .forEach(assignment -> {
                    Permission permission = byId.get(assignment.getPermissionId());
                    if (permission != null && permission.isActive() && !permission.isDeleted()) {
                        result.add(new RolePermissionAssignmentView(permission.getUuid(), permission.getName().value(), permission.getResource().value(), permission.getAction().value(), assignment.getAssignedAt(), assignment.getExpiresAt()));
                    }
                });
        return List.copyOf(result);
    }
}
