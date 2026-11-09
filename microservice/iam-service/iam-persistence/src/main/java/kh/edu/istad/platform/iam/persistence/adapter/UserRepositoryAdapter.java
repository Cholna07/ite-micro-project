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

import java.time.Instant;
import java.util.List;
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
    public List<User> findAll(UserFilterQuery query) {
        return userJpaRepository.findAll()
                .stream()
                .map(userPersistenceMapper::toDomain)
                .filter(user -> query.keyword() == null
                        || query.keyword().isBlank()
                        || (user.getUsername() != null
                        && user.getUsername().value()
                        .toLowerCase().contains(query.keyword().toLowerCase()))
                        || (user.getEmail() != null
                        && user.getEmail().value()
                        .toLowerCase().contains(query.keyword().toLowerCase())))
                .filter(user -> query.enabled() == null
                        || user.isEnabled() == query.enabled())
                .filter(user -> query.emailVerified() == null
                        || user.isEmailVerified() == query.emailVerified())
                .filter(user -> query.deleted() == null
                        || user.isDeleted() == query.deleted())
                .toList();
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