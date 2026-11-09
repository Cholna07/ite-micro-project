package kh.edu.istad.platform.iam.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.UserPermission;
import kh.edu.istad.platform.iam.persistence.entity.UserPermissionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserPermissionPersistenceMapper {

    public UserPermission toDomain(UserPermissionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        UserPermission userPermission = new UserPermission();

        userPermission.setId(entity.getId());
        userPermission.setUserId(entity.getUserId());
        userPermission.setPermissionId(entity.getPermissionId());
        userPermission.setEffect(entity.getEffect());
        userPermission.setAssignedAt(entity.getAssignedAt());
        userPermission.setAssignedBy(entity.getAssignedBy());
        userPermission.setExpiresAt(entity.getExpiresAt());
        userPermission.setRevokedAt(entity.getRevokedAt());
        userPermission.setRevokedBy(entity.getRevokedBy());
        userPermission.setReason(entity.getReason());

        return userPermission;
    }

    public UserPermissionJpaEntity toEntity(UserPermission userPermission) {
        if (userPermission == null) {
            return null;
        }

        UserPermissionJpaEntity entity = new UserPermissionJpaEntity();

        entity.setId(userPermission.getId());
        entity.setUserId(userPermission.getUserId());
        entity.setPermissionId(userPermission.getPermissionId());
        entity.setEffect(userPermission.getEffect());
        entity.setAssignedBy(userPermission.getAssignedBy());
        entity.setExpiresAt(userPermission.getExpiresAt());
        entity.setRevokedAt(userPermission.getRevokedAt());
        entity.setRevokedBy(userPermission.getRevokedBy());
        entity.setReason(userPermission.getReason());

        return entity;
    }
}