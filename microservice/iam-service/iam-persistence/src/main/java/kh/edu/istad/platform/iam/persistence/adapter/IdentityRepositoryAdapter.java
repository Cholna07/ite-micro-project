package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.iam.domain.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.domain.valueobject.IdentityProvider;
import kh.edu.istad.platform.iam.persistence.entity.IdentityJpaEntity;
import kh.edu.istad.platform.iam.persistence.mapper.IdentityPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.IdentityJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class IdentityRepositoryAdapter implements IdentityRepository {

    private final IdentityJpaRepository identityJpaRepository;
    private final IdentityPersistenceMapper identityPersistenceMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Identity> findByUuid(UUID uuid) {
        return identityJpaRepository.findByUuid(uuid)
                .map(identityPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Identity> findByProviderAndProviderSubject(
            String provider,
            String providerSubject
    ) {
        IdentityProvider identityProvider = IdentityProvider.valueOf(
                provider.trim().toUpperCase()
        );

        return identityJpaRepository
                .findByProviderAndProviderSubject(String.valueOf(identityProvider), providerSubject)
                .map(identityPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Identity> findByUserId(Long userId) {
        return identityJpaRepository.findByUserId(userId)
                .stream()
                .map(identityPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Identity save(Identity identity) {
        IdentityJpaEntity entity;

        if (identity.getId() == null) {
            entity = identityPersistenceMapper.toEntity(identity);

            if (entity.getUuid() == null) {
                entity.setUuid(UUID.randomUUID());
            }
        } else {
            entity = identityJpaRepository.findById(identity.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Identity not found: " + identity.getId()
                    ));

            IdentityJpaEntity updatedEntity =
                    identityPersistenceMapper.toEntity(identity);

            entity.setUserId(updatedEntity.getUserId());
            entity.setProvider(updatedEntity.getProvider());
            entity.setProviderSubject(updatedEntity.getProviderSubject());
            entity.setEmail(updatedEntity.getEmail());
            entity.setEmailVerified(updatedEntity.isEmailVerified());
            entity.setDisplayName(updatedEntity.getDisplayName());
            entity.setUsername(updatedEntity.getUsername());
            entity.setPictureUrl(updatedEntity.getPictureUrl());
            entity.setAttributes(updatedEntity.getAttributes());
            entity.setLinkedAt(updatedEntity.getLinkedAt());
            entity.setLastLoginAt(updatedEntity.getLastLoginAt());
            entity.setDeleted(updatedEntity.isDeleted());
            entity.setDeletedAt(updatedEntity.getDeletedAt());
            entity.setDeletedBy(updatedEntity.getDeletedBy());
        }

        IdentityJpaEntity savedEntity = identityJpaRepository.save(entity);

        return identityPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProviderAndProviderSubject(
            String provider,
            String providerSubject
    ) {
        IdentityProvider identityProvider = IdentityProvider.valueOf(
                provider.trim().toUpperCase()
        );

        return identityJpaRepository.existsByProviderAndProviderSubject(
                String.valueOf(identityProvider),
                providerSubject
        );
    }
}