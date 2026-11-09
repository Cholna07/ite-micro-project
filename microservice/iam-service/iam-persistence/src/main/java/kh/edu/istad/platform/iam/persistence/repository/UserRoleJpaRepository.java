package kh.edu.istad.platform.iam.persistence.repository;

import kh.edu.istad.platform.iam.persistence.entity.UserRoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleJpaEntity, Long> {

    List<UserRoleJpaEntity> findByUserId(Long userId);

    @Query("""
            SELECT ur
            FROM UserRoleJpaEntity ur
            WHERE ur.userId = :userId
              AND ur.roleId = :roleId
              AND ur.revokedAt IS NULL
              AND (ur.expiresAt IS NULL OR ur.expiresAt > :now)
            """)
    Optional<UserRoleJpaEntity> findActiveAssignment(
            @Param("userId") Long userId,
            @Param("roleId") Long roleId,
            @Param("now") Instant now
    );

    @Query("""
            SELECT COUNT(ur) > 0
            FROM UserRoleJpaEntity ur
            WHERE ur.userId = :userId
              AND ur.roleId = :roleId
              AND ur.revokedAt IS NULL
              AND (ur.expiresAt IS NULL OR ur.expiresAt > :now)
            """)
    boolean existsActiveAssignment(
            @Param("userId") Long userId,
            @Param("roleId") Long roleId,
            @Param("now") Instant now
    );

    @Query("""
            SELECT ur
            FROM UserRoleJpaEntity ur
            WHERE ur.userId = :userId
              AND ur.revokedAt IS NULL
              AND (ur.expiresAt IS NULL OR ur.expiresAt > :now)
            """)
    List<UserRoleJpaEntity> findActiveAssignmentsByUserId(
            @Param("userId") Long userId,
            @Param("now") Instant now
    );
}
