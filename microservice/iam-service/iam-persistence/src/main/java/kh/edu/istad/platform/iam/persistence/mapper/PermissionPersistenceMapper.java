package kh.edu.istad.platform.iam.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.valueobject.Action;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionName;
import kh.edu.istad.platform.iam.domain.valueobject.Resource;
import kh.edu.istad.platform.iam.persistence.entity.PermissionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PermissionPersistenceMapper {

    public Permission toDomain(PermissionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Permission permission = new Permission();

        permission.setId(entity.getId());
        permission.setUuid(entity.getUuid());
        permission.setName(entity.getName() == null
                ? null : new PermissionName(entity.getName()));
        permission.setResource(entity.getResource() == null
                ? null : new Resource(entity.getResource()));
        permission.setAction(entity.getAction() == null
                ? null : new Action(entity.getAction()));
        permission.setDescription(entity.getDescription());
        permission.setSystem(entity.isSystem());
        permission.setActive(entity.isActive());
        permission.setDeleted(entity.isDeleted());
        permission.setDeletedAt(entity.getDeletedAt());
        permission.setDeletedBy(entity.getDeletedBy());
        permission.setCreatedAt(entity.getCreatedAt());
        permission.setCreatedBy(entity.getCreatedBy());
        permission.setUpdatedAt(entity.getUpdatedAt());
        permission.setUpdatedBy(entity.getUpdatedBy());
        permission.setVersion(entity.getVersion());

        return permission;
    }

    public PermissionJpaEntity toEntity(Permission permission) {
        if (permission == null) {
            return null;
        }

        PermissionJpaEntity entity = new PermissionJpaEntity();

        entity.setId(permission.getId());
        entity.setUuid(permission.getUuid());
        entity.setName(permission.getName() == null
                ? null : permission.getName().value());
        entity.setResource(permission.getResource() == null
                ? null : permission.getResource().value());
        entity.setAction(permission.getAction() == null
                ? null : permission.getAction().value());
        entity.setDescription(permission.getDescription());
        entity.setSystem(permission.isSystem());
        entity.setActive(permission.isActive());
        entity.setDeleted(permission.isDeleted());
        entity.setDeletedAt(permission.getDeletedAt());
        entity.setDeletedBy(permission.getDeletedBy());

        return entity;
    }
}