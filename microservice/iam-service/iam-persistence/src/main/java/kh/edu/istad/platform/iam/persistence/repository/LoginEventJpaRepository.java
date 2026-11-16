package kh.edu.istad.platform.iam.persistence.repository;

import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;
import kh.edu.istad.platform.iam.persistence.entity.LoginEventJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface LoginEventJpaRepository extends JpaRepository<LoginEventJpaEntity, UUID> {
    @EntityGraph(attributePaths = "user")
    @Query("select e from LoginEventJpaEntity e "
            + "where (:userUuid is null or e.user.uuid = :userUuid) "
            + "and (:outcome is null or e.outcome = :outcome)")
    Page<LoginEventJpaEntity> findHistory(@Param("userUuid") UUID userUuid,
                                         @Param("outcome") LoginEventOutcome outcome,
                                         Pageable pageable);
}
