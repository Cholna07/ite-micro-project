package kh.edu.istad.platform.iam.persistence.repository;

import kh.edu.istad.platform.iam.persistence.entity.RolePermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionJpaEntity, Long> {

    List<RolePermissionJpaEntity> findByRoleId(Long roleId);

    @Query("""
            SELECT rp
            FROM RolePermissionJpaEntity rp
            WHERE rp.roleId = :roleId
              AND rp.revokedAt IS NULL
              AND (rp.expiresAt IS NULL OR rp.expiresAt > :now)
            """)
    List<RolePermissionJpaEntity> findActiveAssignmentsByRoleId(
            @Param("roleId") Long roleId,
            @Param("now") Instant now
    );

    @Query("""
            SELECT rp
            FROM RolePermissionJpaEntity rp
            WHERE rp.roleId = :roleId
              AND rp.permissionId = :permissionId
              AND rp.revokedAt IS NULL
              AND (rp.expiresAt IS NULL OR rp.expiresAt > :now)
            """)
    Optional<RolePermissionJpaEntity> findActiveAssignment(
            @Param("roleId") Long roleId,
            @Param("permissionId") Long permissionId,
            @Param("now") Instant now
    );

    @Query("""
            SELECT COUNT(rp) > 0
            FROM RolePermissionJpaEntity rp
            WHERE rp.roleId = :roleId
              AND rp.permissionId = :permissionId
              AND rp.revokedAt IS NULL
              AND (rp.expiresAt IS NULL OR rp.expiresAt > :now)
            """)
    boolean existsActiveAssignment(
            @Param("roleId") Long roleId,
            @Param("permissionId") Long permissionId,
            @Param("now") Instant now
    );
}
