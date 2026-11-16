package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.dto.query.PermissionFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.persistence.entity.PermissionJpaEntity;
import kh.edu.istad.platform.iam.persistence.mapper.PermissionPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.PermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

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
    @Transactional(readOnly = true)
    public PageResult<Permission> findPage(PermissionFilterQuery query) {
        Page<PermissionJpaEntity> page = permissionJpaRepository.findAll(
                specification(query), PageRequest.of(query.page(), query.size(), sort(query)));
        return new PageResult<>(page.getContent().stream().map(permissionPersistenceMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast());
    }

    Specification<PermissionJpaEntity> specification(PermissionFilterQuery query) {
        return (root, criteria, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.keyword() != null && !query.keyword().isBlank()) {
                String pattern = "%" + escapeLike(query.keyword().trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), pattern, '\\'),
                        builder.like(builder.lower(root.get("resource")), pattern, '\\'),
                        builder.like(builder.lower(root.get("action")), pattern, '\\'),
                        builder.like(builder.lower(root.get("description")), pattern, '\\')));
            }
            if (query.resource() != null && !query.resource().isBlank())
                predicates.add(builder.equal(builder.lower(root.get("resource")), query.resource().trim().toLowerCase(Locale.ROOT)));
            if (query.action() != null && !query.action().isBlank())
                predicates.add(builder.equal(builder.lower(root.get("action")), query.action().trim().toLowerCase(Locale.ROOT)));
            if (!Boolean.TRUE.equals(query.deleted())) predicates.add(builder.isFalse(root.get("isDeleted")));
            else predicates.add(builder.isTrue(root.get("isDeleted")));
            if (query.active() != null) predicates.add(builder.equal(root.get("isActive"), query.active()));
            if (query.systemPermission() != null) predicates.add(builder.equal(root.get("isSystem"), query.systemPermission()));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Sort sort(PermissionFilterQuery query) {
        Map<String, String> allowed = Map.of("name", "name", "resource", "resource", "action", "action",
                "createdAt", "createdAt", "updatedAt", "updatedAt", "active", "isActive",
                "systemPermission", "isSystem");
        String property = allowed.get(query.sortBy());
        Sort.Direction direction = "DESC".equalsIgnoreCase(query.sortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
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

        PermissionJpaEntity savedEntity = permissionJpaRepository.saveAndFlush(entity);

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
