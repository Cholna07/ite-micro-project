package kh.edu.istad.platform.iam.domain.port.out;

import kh.edu.istad.platform.iam.domain.entity.RolePermission;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository {

    RolePermission save(RolePermission rolePermission);

    List<RolePermission> findByRoleId(Long roleId);

    List<RolePermission> findActiveAssignmentsByRoleId(Long roleId);

    Optional<RolePermission> findActiveAssignment(Long roleId, Long permissionId);

    boolean existsActiveAssignment(Long roleId, Long permissionId);
}
