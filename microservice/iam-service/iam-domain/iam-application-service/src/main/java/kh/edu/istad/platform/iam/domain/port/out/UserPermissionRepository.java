package kh.edu.istad.platform.iam.domain.port.out;

import kh.edu.istad.platform.iam.domain.entity.UserPermission;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserPermissionRepository {

    UserPermission save(UserPermission userPermission);
    int revokeExpiredAssignments(Long userId, Long permissionId, Instant now, String actor);
    List<UserPermission> findByUserId(Long userId);
    boolean existsActiveAssignment(Long userId, Long permissionId);
    Optional<UserPermission> findActiveAssignment(Long userId, Long permissionId);
    List<UserPermission> findActiveAssignmentsByUserId(Long userId);
}
