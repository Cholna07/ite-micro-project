package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.dto.query.UserFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.persistence.entity.UserJpaEntity;
import kh.edu.istad.platform.iam.persistence.mapper.UserPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserPersistenceMapper userPersistenceMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUuid(UUID uuid) {
        return userJpaRepository.findByUuid(uuid)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userJpaRepository
                .findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<User> findAll(UserFilterQuery query) {
        if (query.page() < 0 || query.size() < 1 || query.size() > 100) {
            throw new IllegalArgumentException("Invalid user list page or size");
        }
        Page<UserJpaEntity> page = userJpaRepository.findAll(specification(query),
                PageRequest.of(query.page(), query.size(), sort(query)));
        return new PageResult<>(page.getContent().stream().map(userPersistenceMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast());
    }

    Specification<UserJpaEntity> specification(UserFilterQuery query) {
        return (root, criteria, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (query.keyword() != null && !query.keyword().isBlank()) {
                String pattern = "%" + escapeLike(query.keyword().trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(builder.or(builder.like(builder.lower(root.get("username")), pattern, '\\'),
                        builder.like(builder.lower(root.get("email")), pattern, '\\')));
            }
            if (query.enabled() != null) predicates.add(builder.equal(root.get("enabled"), query.enabled()));
            if (query.locked() != null) predicates.add(builder.equal(root.get("accountNonLocked"), !query.locked()));
            if (query.emailVerified() != null) predicates.add(builder.equal(root.get("emailVerified"), query.emailVerified()));
            if (query.deleted() != null) predicates.add(builder.equal(root.get("isDeleted"), query.deleted()));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Sort sort(UserFilterQuery query) {
        Map<String, String> allowed = Map.of("username", "username", "email", "email",
                "givenName", "givenName", "familyName", "familyName", "createdAt", "createdAt",
                "enabled", "enabled", "emailVerified", "emailVerified");
        String property = query.sortBy() == null ? "username" : allowed.getOrDefault(query.sortBy(), "username");
        Sort.Direction direction = "DESC".equalsIgnoreCase(query.sortDirection())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity;

        if (user.getId() == null) {
            entity = userPersistenceMapper.toEntity(user);

            if (entity.getUuid() == null) {
                entity.setUuid(UUID.randomUUID());
            }
        } else {
            entity = userJpaRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User not found: " + user.getId()
                    ));

            UserJpaEntity updatedEntity = userPersistenceMapper.toEntity(user);

            entity.setUsername(updatedEntity.getUsername());
            entity.setEmail(updatedEntity.getEmail());
            entity.setEmailVerified(updatedEntity.isEmailVerified());
            entity.setPasswordHash(updatedEntity.getPasswordHash());
            entity.setGivenName(updatedEntity.getGivenName());
            entity.setFamilyName(updatedEntity.getFamilyName());
            entity.setMiddleName(updatedEntity.getMiddleName());
            entity.setNickname(updatedEntity.getNickname());
            entity.setGender(updatedEntity.getGender());
            entity.setBirthdate(updatedEntity.getBirthdate());
            entity.setProfileUrl(updatedEntity.getProfileUrl());
            entity.setPictureUrl(updatedEntity.getPictureUrl());
            entity.setWebsiteUrl(updatedEntity.getWebsiteUrl());
            entity.setZoneInfo(updatedEntity.getZoneInfo());
            entity.setLocale(updatedEntity.getLocale());
            entity.setPhoneNumber(updatedEntity.getPhoneNumber());
            entity.setPhoneNumberVerified(updatedEntity.isPhoneNumberVerified());
            entity.setStreetAddress(updatedEntity.getStreetAddress());
            entity.setLocality(updatedEntity.getLocality());
            entity.setRegion(updatedEntity.getRegion());
            entity.setPostalCode(updatedEntity.getPostalCode());
            entity.setCountry(updatedEntity.getCountry());
            entity.setEnabled(updatedEntity.isEnabled());
            entity.setAccountNonExpired(updatedEntity.isAccountNonExpired());
            entity.setAccountNonLocked(updatedEntity.isAccountNonLocked());
            entity.setCredentialsNonExpired(updatedEntity.isCredentialsNonExpired());
            entity.setFailedLoginAttempts(updatedEntity.getFailedLoginAttempts());
            entity.setLastLoginAt(updatedEntity.getLastLoginAt());
            entity.setPasswordChangedAt(updatedEntity.getPasswordChangedAt());
            entity.setDeleted(updatedEntity.isDeleted());
            entity.setDeletedAt(updatedEntity.getDeletedAt());
            entity.setDeletedBy(updatedEntity.getDeletedBy());
        }

        UserJpaEntity savedEntity = userJpaRepository.save(entity);

        return userPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(User user) {
        UserJpaEntity entity = userJpaRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + user.getId()
                ));

        entity.setDeleted(true);
        entity.setDeletedAt(Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userJpaRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}
