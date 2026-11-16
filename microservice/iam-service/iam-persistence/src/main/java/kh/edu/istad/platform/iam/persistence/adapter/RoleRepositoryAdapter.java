package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.dto.query.RoleFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.persistence.entity.RoleJpaEntity;
import kh.edu.istad.platform.iam.persistence.mapper.RolePersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.RoleJpaRepository;
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
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
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
    @Transactional(readOnly = true)
    public PageResult<Role> findPage(RoleFilterQuery query) {
        Page<RoleJpaEntity> page = roleJpaRepository.findAll(
                specification(query), PageRequest.of(query.page(), query.size(), sort(query)));
        return new PageResult<>(page.getContent().stream().map(rolePersistenceMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast());
    }

    Specification<RoleJpaEntity> specification(RoleFilterQuery query) {
        return (root, criteria, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.keyword() != null && !query.keyword().isBlank()) {
                String pattern = "%" + escapeLike(query.keyword().trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), pattern, '\\'),
                        builder.like(builder.lower(root.get("description")), pattern, '\\')));
            }
            if (!Boolean.TRUE.equals(query.deleted())) predicates.add(builder.isFalse(root.get("isDeleted")));
            if (query.active() != null) predicates.add(builder.equal(root.get("isActive"), query.active()));
            if (query.systemRole() != null) predicates.add(builder.equal(root.get("isSystem"), query.systemRole()));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Sort sort(RoleFilterQuery query) {
        Map<String, String> allowed = Map.of("name", "name", "description", "description",
                "createdAt", "createdAt", "updatedAt", "updatedAt", "active", "isActive",
                "systemRole", "isSystem");
        String property = allowed.get(query.sortBy());
        Sort.Direction direction = "DESC".equalsIgnoreCase(query.sortDirection())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
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

        RoleJpaEntity savedEntity = roleJpaRepository.saveAndFlush(entity);

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
