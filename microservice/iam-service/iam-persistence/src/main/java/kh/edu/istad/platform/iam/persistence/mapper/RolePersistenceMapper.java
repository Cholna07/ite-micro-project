package kh.edu.istad.platform.iam.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import kh.edu.istad.platform.iam.persistence.entity.RoleJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RolePersistenceMapper {

    public Role toDomain(RoleJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Role role = new Role();

        role.setId(entity.getId());
        role.setUuid(entity.getUuid());
        role.setName(entity.getName() == null
                ? null : new RoleName(entity.getName()));
        role.setDescription(entity.getDescription());
        role.setSystem(entity.isSystem());
        role.setActive(entity.isActive());
        role.setDeleted(entity.isDeleted());
        role.setDeletedAt(entity.getDeletedAt());
        role.setDeletedBy(entity.getDeletedBy());
        role.setCreatedAt(entity.getCreatedAt());
        role.setCreatedBy(entity.getCreatedBy());
        role.setUpdatedAt(entity.getUpdatedAt());
        role.setUpdatedBy(entity.getUpdatedBy());
        role.setVersion(entity.getVersion());

        return role;
    }

    public RoleJpaEntity toEntity(Role role) {
        if (role == null) {
            return null;
        }

        RoleJpaEntity entity = new RoleJpaEntity();

        entity.setId(role.getId());
        entity.setUuid(role.getUuid());
        entity.setName(role.getName() == null
                ? null : role.getName().value());
        entity.setDescription(role.getDescription());
        entity.setSystem(role.isSystem());
        entity.setActive(role.isActive());
        entity.setDeleted(role.isDeleted());
        entity.setDeletedAt(role.getDeletedAt());
        entity.setDeletedBy(role.getDeletedBy());

        return entity;
    }
}