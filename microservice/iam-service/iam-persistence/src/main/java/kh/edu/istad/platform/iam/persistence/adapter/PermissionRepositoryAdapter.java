package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.persistence.entity.PermissionJpaEntity;
import kh.edu.istad.platform.iam.persistence.mapper.PermissionPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.PermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class PermissionRepositoryAdapter implements PermissionRepository {

    private final PermissionJpaRepository permissionJpaRepository;
    private final PermissionPersistenceMapper permissionPersistenceMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Permission> findByUuid(UUID uuid) {
        return permissionJpaRepository.findByUuid(uuid)
                .map(permissionPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Permission> findByName(String name) {
        return permissionJpaRepository.findByName(name)
                .map(permissionPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> findAll() {
        return permissionJpaRepository.findAll()
                .stream()
                .map(permissionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Permission save(Permission permission) {
        PermissionJpaEntity entity;

        if (permission.getId() == null) {
            entity = permissionPersistenceMapper.toEntity(permission);

            if (entity.getUuid() == null) {
                entity.setUuid(UUID.randomUUID());
            }
        } else {
            entity = permissionJpaRepository.findById(permission.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Permission not found: " + permission.getId()
                    ));

            PermissionJpaEntity updatedEntity =
                    permissionPersistenceMapper.toEntity(permission);

            entity.setName(updatedEntity.getName());
            entity.setResource(updatedEntity.getResource());
            entity.setAction(updatedEntity.getAction());
            entity.setDescription(updatedEntity.getDescription());
            entity.setSystem(updatedEntity.isSystem());
            entity.setActive(updatedEntity.isActive());
            entity.setDeleted(updatedEntity.isDeleted());
            entity.setDeletedAt(updatedEntity.getDeletedAt());
            entity.setDeletedBy(updatedEntity.getDeletedBy());
        }

        PermissionJpaEntity savedEntity = permissionJpaRepository.save(entity);

        return permissionPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(Permission permission) {
        PermissionJpaEntity entity = permissionJpaRepository.findById(permission.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Permission not found: " + permission.getId()
                ));

        entity.setDeleted(true);
        entity.setDeletedAt(Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return permissionJpaRepository.existsByName(name);
    }
}