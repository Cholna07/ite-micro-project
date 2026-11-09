package kh.edu.istad.platform.iam.persistence.repository;

import kh.edu.istad.platform.iam.persistence.entity.UserPermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserPermissionJpaRepository extends JpaRepository<UserPermissionJpaEntity, Long> {

    List<UserPermissionJpaEntity> findByUserId(Long userId);

    @Query("""
            SELECT COUNT(up) > 0
            FROM UserPermissionJpaEntity up
            WHERE up.userId = :userId
              AND up.permissionId = :permissionId
              AND up.revokedAt IS NULL
              AND (up.expiresAt IS NULL OR up.expiresAt > :now)
            """)
    boolean existsActiveAssignment(
            @Param("userId") Long userId,
            @Param("permissionId") Long permissionId,
            @Param("now") Instant now
    );

    @Query("""
            SELECT up
            FROM UserPermissionJpaEntity up
            WHERE up.userId = :userId
              AND up.permissionId = :permissionId
              AND up.revokedAt IS NULL
              AND (up.expiresAt IS NULL OR up.expiresAt > :now)
            """)
    Optional<UserPermissionJpaEntity> findActiveAssignment(
            @Param("userId") Long userId,
            @Param("permissionId") Long permissionId,
            @Param("now") Instant now
    );

    @Query("""
            SELECT up
            FROM UserPermissionJpaEntity up
            WHERE up.userId = :userId
              AND up.revokedAt IS NULL
              AND (up.expiresAt IS NULL OR up.expiresAt > :now)
            """)
    List<UserPermissionJpaEntity> findActiveAssignmentsByUserId(
            @Param("userId") Long userId,
            @Param("now") Instant now
    );
}
