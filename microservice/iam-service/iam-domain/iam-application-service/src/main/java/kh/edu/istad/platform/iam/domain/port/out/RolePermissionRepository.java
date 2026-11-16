package kh.edu.istad.platform.iam.domain.port.out;

import kh.edu.istad.platform.iam.domain.entity.RolePermission;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository {

    RolePermission save(RolePermission rolePermission);
    int revokeExpiredAssignments(Long roleId, Long permissionId, Instant now, String actor);

    List<RolePermission> findByRoleId(Long roleId);

    List<RolePermission> findActiveAssignmentsByRoleId(Long roleId);

    Optional<RolePermission> findActiveAssignment(Long roleId, Long permissionId);

    boolean existsActiveAssignment(Long roleId, Long permissionId);
}
