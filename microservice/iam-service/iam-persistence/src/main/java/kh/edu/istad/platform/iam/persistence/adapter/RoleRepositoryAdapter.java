package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.persistence.entity.RoleJpaEntity;
import kh.edu.istad.platform.iam.persistence.mapper.RolePersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.RoleJpaRepository;
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
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;
    private final RolePersistenceMapper rolePersistenceMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByUuid(UUID uuid) {
        return roleJpaRepository.findByUuid(uuid)
                .map(rolePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        return roleJpaRepository.findByName(name)
                .map(rolePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleJpaRepository.findAll()
                .stream()
                .map(rolePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Role save(Role role) {
        RoleJpaEntity entity;

        if (role.getId() == null) {
            entity = rolePersistenceMapper.toEntity(role);

            if (entity.getUuid() == null) {
                entity.setUuid(UUID.randomUUID());
            }
        } else {
            entity = roleJpaRepository.findById(role.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Role not found: " + role.getId()
                    ));

            RoleJpaEntity updatedEntity = rolePersistenceMapper.toEntity(role);

            entity.setName(updatedEntity.getName());
            entity.setDescription(updatedEntity.getDescription());
            entity.setSystem(updatedEntity.isSystem());
            entity.setActive(updatedEntity.isActive());
            entity.setDeleted(updatedEntity.isDeleted());
            entity.setDeletedAt(updatedEntity.getDeletedAt());
            entity.setDeletedBy(updatedEntity.getDeletedBy());
        }

        RoleJpaEntity savedEntity = roleJpaRepository.save(entity);

        return rolePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(Role role) {
        RoleJpaEntity entity = roleJpaRepository.findById(role.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Role not found: " + role.getId()
                ));

        entity.setDeleted(true);
        entity.setDeletedAt(Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return roleJpaRepository.existsByName(name);
    }
}