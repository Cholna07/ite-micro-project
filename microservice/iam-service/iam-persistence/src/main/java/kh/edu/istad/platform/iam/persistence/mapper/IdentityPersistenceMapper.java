package kh.edu.istad.platform.iam.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.iam.domain.valueobject.Email;
import kh.edu.istad.platform.iam.domain.valueobject.ProviderSubject;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import kh.edu.istad.platform.iam.persistence.entity.IdentityJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class IdentityPersistenceMapper {

    public Identity toDomain(IdentityJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Identity identity = new Identity();

        identity.setId(entity.getId());
        identity.setUuid(entity.getUuid());
        identity.setUserId(entity.getUserId());
        identity.setProvider(entity.getProvider());
        identity.setProviderSubject(entity.getProviderSubject() == null
                ? null : new ProviderSubject(entity.getProviderSubject()));
        identity.setEmail(entity.getEmail() == null
                ? null : new Email(entity.getEmail()));
        identity.setEmailVerified(entity.isEmailVerified());
        identity.setDisplayName(entity.getDisplayName());
        identity.setUsername(entity.getUsername() == null
                ? null : new Username(entity.getUsername()));
        identity.setPictureUrl(entity.getPictureUrl());
        identity.setAttributes(entity.getAttributes());
        identity.setLinkedAt(entity.getLinkedAt());
        identity.setLastLoginAt(entity.getLastLoginAt());
        identity.setDeleted(entity.isDeleted());
        identity.setDeletedAt(entity.getDeletedAt());
        identity.setDeletedBy(entity.getDeletedBy());
        identity.setCreatedAt(entity.getCreatedAt());
        identity.setCreatedBy(entity.getCreatedBy());
        identity.setUpdatedAt(entity.getUpdatedAt());
        identity.setUpdatedBy(entity.getUpdatedBy());
        identity.setVersion(entity.getVersion());

        return identity;
    }

    public IdentityJpaEntity toEntity(Identity identity) {
        if (identity == null) {
            return null;
        }

        IdentityJpaEntity entity = new IdentityJpaEntity();

        entity.setId(identity.getId());
        entity.setUuid(identity.getUuid());
        entity.setUserId(identity.getUserId());
        entity.setProvider(identity.getProvider());
        entity.setProviderSubject(identity.getProviderSubject() == null
                ? null : identity.getProviderSubject().value());
        entity.setEmail(identity.getEmail() == null
                ? null : identity.getEmail().value());
        entity.setEmailVerified(identity.isEmailVerified());
        entity.setDisplayName(identity.getDisplayName());
        entity.setUsername(identity.getUsername() == null
                ? null : identity.getUsername().value());
        entity.setPictureUrl(identity.getPictureUrl());
        entity.setAttributes(identity.getAttributes());
        entity.setLinkedAt(identity.getLinkedAt());
        entity.setLastLoginAt(identity.getLastLoginAt());
        entity.setDeleted(identity.isDeleted());
        entity.setDeletedAt(identity.getDeletedAt());
        entity.setDeletedBy(identity.getDeletedBy());

        return entity;
    }
}