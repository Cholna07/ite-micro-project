package kh.edu.istad.platform.iam.persistence.repository;

import kh.edu.istad.platform.iam.persistence.entity.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, Long>, JpaSpecificationExecutor<PermissionJpaEntity> {

    Optional<PermissionJpaEntity> findByUuid(UUID uuid);

    Optional<PermissionJpaEntity> findByName(String name);

    boolean existsByName(String name);
}
