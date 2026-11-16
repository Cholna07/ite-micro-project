package kh.edu.istad.platform.iam.domain.port.out;

import kh.edu.istad.platform.iam.domain.entity.UserRole;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface UserRoleRepository {
    UserRole save(UserRole userRole);
    List<UserRole> findByUserId(Long userId);
    Optional<UserRole> findActiveAssignment(Long userId, Long roleId);
    boolean existsActiveAssignment(Long userId, Long roleId);
    int revokeExpiredAssignments(Long userId, Long roleId, Instant now, String actor);
    List<UserRole> findActiveAssignmentsByUserId(Long userId);
}
