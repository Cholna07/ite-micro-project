package kh.edu.istad.platform.iam.persistence.repository;

import kh.edu.istad.platform.iam.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long>, JpaSpecificationExecutor<UserJpaEntity> {

    Optional<UserJpaEntity> findByUuid(UUID uuid);

    Optional<UserJpaEntity> findByUsername(String username);

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
