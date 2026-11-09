package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.entity.RolePermission;
import kh.edu.istad.platform.iam.domain.port.out.RolePermissionRepository;
import kh.edu.istad.platform.iam.persistence.entity.RolePermissionJpaEntity;
import kh.edu.istad.platform.iam.persistence.mapper.RolePermissionPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.RolePermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class RolePermissionRepositoryAdapter implements RolePermissionRepository {

    private final RolePermissionJpaRepository rolePermissionJpaRepository;
    private final RolePermissionPersistenceMapper rolePermissionPersistenceMapper;

    @Override
    public RolePermission save(RolePermission rolePermission) {
        RolePermissionJpaEntity entity;

        if (rolePermission.getId() == null) {
            entity = rolePermissionPersistenceMapper.toEntity(rolePermission);
        } else {
            entity = rolePermissionJpaRepository.findById(rolePermission.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Role permission assignment not found: " + rolePermission.getId()
                    ));

            entity.setRoleId(rolePermission.getRoleId());
            entity.setPermissionId(rolePermission.getPermissionId());
            entity.setAssignedBy(rolePermission.getAssignedBy());
            entity.setExpiresAt(rolePermission.getExpiresAt());
            entity.setRevokedAt(rolePermission.getRevokedAt());
            entity.setRevokedBy(rolePermission.getRevokedBy());
            entity.setReason(rolePermission.getReason());
        }

        RolePermissionJpaEntity savedEntity =
                rolePermissionJpaRepository.save(entity);

        return rolePermissionPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePermission> findByRoleId(Long roleId) {
        return rolePermissionJpaRepository.findByRoleId(roleId)
                .stream()
                .map(rolePermissionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePermission> findActiveAssignmentsByRoleId(Long roleId) {
        return rolePermissionJpaRepository
                .findActiveAssignmentsByRoleId(roleId, Instant.now())
                .stream()
                .map(rolePermissionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RolePermission> findActiveAssignment(
            Long roleId,
            Long permissionId
    ) {
        return rolePermissionJpaRepository
                .findActiveAssignment(roleId, permissionId, Instant.now())
                .map(rolePermissionPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveAssignment(Long roleId, Long permissionId) {
        return rolePermissionJpaRepository.existsActiveAssignment(
                roleId,
                permissionId,
                Instant.now()
        );
    }
}