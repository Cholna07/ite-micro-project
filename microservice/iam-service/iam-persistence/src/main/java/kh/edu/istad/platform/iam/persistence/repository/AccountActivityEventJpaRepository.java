package kh.edu.istad.platform.iam.persistence.repository;

import kh.edu.istad.platform.iam.persistence.entity.AccountActivityEventJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AccountActivityEventJpaRepository extends JpaRepository<AccountActivityEventJpaEntity, UUID> {
    @EntityGraph(attributePaths = "user")
    @Query("select event from AccountActivityEventJpaEntity event where event.user.uuid = :userUuid")
    Page<AccountActivityEventJpaEntity> findByUserUuid(@Param("userUuid") UUID userUuid, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query("select event from AccountActivityEventJpaEntity event")
    Page<AccountActivityEventJpaEntity> findAllActivities(Pageable pageable);
}
