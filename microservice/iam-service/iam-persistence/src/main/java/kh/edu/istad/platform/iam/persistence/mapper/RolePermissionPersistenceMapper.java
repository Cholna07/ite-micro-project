package kh.edu.istad.platform.iam.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.RolePermission;
import kh.edu.istad.platform.iam.persistence.entity.RolePermissionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionPersistenceMapper {

    public RolePermission toDomain(RolePermissionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        RolePermission rolePermission = new RolePermission();

        rolePermission.setId(entity.getId());
        rolePermission.setRoleId(entity.getRoleId());
        rolePermission.setPermissionId(entity.getPermissionId());
        rolePermission.setAssignedAt(entity.getAssignedAt());
        rolePermission.setAssignedBy(entity.getAssignedBy());
        rolePermission.setExpiresAt(entity.getExpiresAt());
        rolePermission.setRevokedAt(entity.getRevokedAt());
        rolePermission.setRevokedBy(entity.getRevokedBy());
        rolePermission.setReason(entity.getReason());

        return rolePermission;
    }

    public RolePermissionJpaEntity toEntity(RolePermission rolePermission) {
        if (rolePermission == null) {
            return null;
        }

        RolePermissionJpaEntity entity = new RolePermissionJpaEntity();

        entity.setId(rolePermission.getId());
        entity.setRoleId(rolePermission.getRoleId());
        entity.setPermissionId(rolePermission.getPermissionId());
        entity.setAssignedBy(rolePermission.getAssignedBy());
        entity.setExpiresAt(rolePermission.getExpiresAt());
        entity.setRevokedAt(rolePermission.getRevokedAt());
        entity.setRevokedBy(rolePermission.getRevokedBy());
        entity.setReason(rolePermission.getReason());

        return entity;
    }
}