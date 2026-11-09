package kh.edu.istad.platform.iam.persistence.repository;

import kh.edu.istad.platform.iam.persistence.entity.IdentityJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityJpaRepository extends JpaRepository<IdentityJpaEntity, Long> {

    Optional<IdentityJpaEntity> findByUuid(UUID uuid);

    Optional<IdentityJpaEntity> findByProviderAndProviderSubject(
            String provider,
            String providerSubject
    );

    List<IdentityJpaEntity> findByUserId(Long userId);

    boolean existsByProviderAndProviderSubject(
            String provider,
            String providerSubject
    );
}
