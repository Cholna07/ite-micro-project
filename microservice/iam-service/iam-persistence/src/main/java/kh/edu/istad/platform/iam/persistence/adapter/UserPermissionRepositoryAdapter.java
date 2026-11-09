package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.entity.UserPermission;
import kh.edu.istad.platform.iam.domain.port.out.UserPermissionRepository;
import kh.edu.istad.platform.iam.persistence.entity.UserPermissionJpaEntity;
import kh.edu.istad.platform.iam.persistence.mapper.UserPermissionPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.UserPermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserPermissionRepositoryAdapter implements UserPermissionRepository {

    private final UserPermissionJpaRepository userPermissionJpaRepository;
    private final UserPermissionPersistenceMapper userPermissionPersistenceMapper;

    @Override
    public UserPermission save(UserPermission userPermission) {
        UserPermissionJpaEntity entity;

        if (userPermission.getId() == null) {
            entity = userPermissionPersistenceMapper.toEntity(userPermission);
        } else {
            entity = userPermissionJpaRepository.findById(userPermission.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User permission assignment not found: " + userPermission.getId()
                    ));

            entity.setUserId(userPermission.getUserId());
            entity.setPermissionId(userPermission.getPermissionId());
            entity.setEffect(userPermission.getEffect());
            entity.setAssignedBy(userPermission.getAssignedBy());
            entity.setExpiresAt(userPermission.getExpiresAt());
            entity.setRevokedAt(userPermission.getRevokedAt());
            entity.setRevokedBy(userPermission.getRevokedBy());
            entity.setReason(userPermission.getReason());
        }

        UserPermissionJpaEntity savedEntity =
                userPermissionJpaRepository.save(entity);

        return userPermissionPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPermission> findByUserId(Long userId) {
        return userPermissionJpaRepository.findByUserId(userId)
                .stream()
                .map(userPermissionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveAssignment(Long userId, Long permissionId) {
        return userPermissionJpaRepository.existsActiveAssignment(
                userId,
                permissionId,
                Instant.now()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserPermission> findActiveAssignment(
            Long userId,
            Long permissionId
    ) {
        return userPermissionJpaRepository
                .findActiveAssignment(userId, permissionId, Instant.now())
                .map(userPermissionPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPermission> findActiveAssignmentsByUserId(Long userId) {
        return userPermissionJpaRepository
                .findActiveAssignmentsByUserId(userId, Instant.now())
                .stream()
                .map(userPermissionPersistenceMapper::toDomain)
                .toList();
    }
}