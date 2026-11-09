package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.iam.domain.port.out.UserRoleRepository;
import kh.edu.istad.platform.iam.persistence.entity.UserRoleJpaEntity;
import kh.edu.istad.platform.iam.persistence.mapper.UserRolePersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.UserRoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserRoleRepositoryAdapter implements UserRoleRepository {

    private final UserRoleJpaRepository userRoleJpaRepository;
    private final UserRolePersistenceMapper userRolePersistenceMapper;

    @Override
    public UserRole save(UserRole userRole) {
        UserRoleJpaEntity entity;

        if (userRole.getId() == null) {
            entity = userRolePersistenceMapper.toEntity(userRole);
        } else {
            entity = userRoleJpaRepository.findById(userRole.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User role assignment not found: " + userRole.getId()
                    ));

            entity.setUserId(userRole.getUserId());
            entity.setRoleId(userRole.getRoleId());
            entity.setAssignedBy(userRole.getAssignedBy());
            entity.setExpiresAt(userRole.getExpiresAt());
            entity.setRevokedAt(userRole.getRevokedAt());
            entity.setRevokedBy(userRole.getRevokedBy());
            entity.setReason(userRole.getReason());
        }

        UserRoleJpaEntity savedEntity = userRoleJpaRepository.save(entity);

        return userRolePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRole> findByUserId(Long userId) {
        return userRoleJpaRepository.findByUserId(userId)
                .stream()
                .map(userRolePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserRole> findActiveAssignment(Long userId, Long roleId) {
        return userRoleJpaRepository
                .findActiveAssignment(userId, roleId, Instant.now())
                .map(userRolePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveAssignment(Long userId, Long roleId) {
        return userRoleJpaRepository.existsActiveAssignment(
                userId,
                roleId,
                Instant.now()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRole> findActiveAssignmentsByUserId(Long userId) {
        return userRoleJpaRepository
                .findActiveAssignmentsByUserId(userId, Instant.now())
                .stream()
                .map(userRolePersistenceMapper::toDomain)
                .toList();
    }
}