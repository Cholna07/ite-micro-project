package kh.edu.istad.platform.iam.persistence.repository;

import kh.edu.istad.platform.iam.persistence.entity.UserSessionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionJpaEntity, UUID> {
    @Query("select s from UserSessionJpaEntity s where s.user.uuid = :userUuid "
            + "and s.revokedAt is null and (s.expiresAt is null or s.expiresAt > :now)")
    Page<UserSessionJpaEntity> findActiveByUser(@Param("userUuid") UUID userUuid,
                                                @Param("now") Instant now,
                                                Pageable pageable);

    @Query("select (count(s) > 0) from UserSessionJpaEntity s where s.id = :sessionId "
            + "and s.user.uuid = :userUuid and s.revokedAt is null and (s.expiresAt is null or s.expiresAt > :now)")
    boolean existsActive(@Param("sessionId") UUID sessionId,
                         @Param("userUuid") UUID userUuid,
                         @Param("now") Instant now);

    @Query("select s from UserSessionJpaEntity s where s.user.uuid = :userUuid "
            + "and s.revokedAt is null and (s.expiresAt is null or s.expiresAt > :now)")
    List<UserSessionJpaEntity> findActiveForRevocation(@Param("userUuid") UUID userUuid,
                                                       @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserSessionJpaEntity s set s.lastActivityAt = :lastActivityAt, s.expiresAt = :expiresAt "
            + "where s.id = :sessionId and s.revokedAt is null and (s.expiresAt is null or s.expiresAt > :lastActivityAt)")
    int updateActivity(@Param("sessionId") UUID sessionId,
                       @Param("lastActivityAt") Instant lastActivityAt,
                       @Param("expiresAt") Instant expiresAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserSessionJpaEntity s set s.lastActivityAt = :lastActivityAt "
            + "where s.id = :sessionId and s.user.uuid = :userUuid and s.revokedAt is null "
            + "and (s.expiresAt is null or s.expiresAt > :lastActivityAt)")
    int touchActivity(@Param("sessionId") UUID sessionId,
                      @Param("userUuid") UUID userUuid,
                      @Param("lastActivityAt") Instant lastActivityAt);
}
