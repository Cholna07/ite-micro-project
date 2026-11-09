package kh.edu.istad.platform.iam.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.iam.persistence.entity.UserRoleJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserRolePersistenceMapper {

    public UserRole toDomain(UserRoleJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        UserRole userRole = new UserRole();

        userRole.setId(entity.getId());
        userRole.setUserId(entity.getUserId());
        userRole.setRoleId(entity.getRoleId());
        userRole.setAssignedAt(entity.getAssignedAt());
        userRole.setAssignedBy(entity.getAssignedBy());
        userRole.setExpiresAt(entity.getExpiresAt());
        userRole.setRevokedAt(entity.getRevokedAt());
        userRole.setRevokedBy(entity.getRevokedBy());
        userRole.setReason(entity.getReason());

        return userRole;
    }

    public UserRoleJpaEntity toEntity(UserRole userRole) {
        if (userRole == null) {
            return null;
        }

        UserRoleJpaEntity entity = new UserRoleJpaEntity();

        entity.setId(userRole.getId());
        entity.setUserId(userRole.getUserId());
        entity.setRoleId(userRole.getRoleId());
        entity.setAssignedBy(userRole.getAssignedBy());
        entity.setExpiresAt(userRole.getExpiresAt());
        entity.setRevokedAt(userRole.getRevokedAt());
        entity.setRevokedBy(userRole.getRevokedBy());
        entity.setReason(userRole.getReason());

        return entity;
    }
}